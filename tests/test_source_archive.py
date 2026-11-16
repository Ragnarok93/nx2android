"""The APK carries the corresponding source and license notices for GPL native code."""
from pathlib import Path
import tempfile
import unittest
import zipfile

from porter.pipeline import corresponding_source_archive


class SourceArchive(unittest.TestCase):
    def test_archive_contains_native_host_and_license_sources(self):
        with tempfile.TemporaryDirectory(prefix="nx2a_source_") as tmp:
            archive = corresponding_source_archive(Path(tmp) / "source.zip")
            self.assertTrue(archive.is_file())
            with zipfile.ZipFile(archive) as z:
                names = set(z.namelist())
            for name in (
                "LICENSE",
                "NOTICE.md",
                "nxnative/LICENSE",
                "nxnative/LICENSE-MIT-ORIGINAL",
                "nxnative/dm/android/build.py",
                "nxnative/dm/android/dm_framegen.c",
                "nxnative/dm/android/lsfg/lsfg_chain.c",
                "nxnative/dm/android/lsfg/COPYING",
            ):
                self.assertIn(name, names)
            self.assertFalse(any(name.lower().endswith("/lossless.dll") for name in names))


if __name__ == "__main__":
    unittest.main()
