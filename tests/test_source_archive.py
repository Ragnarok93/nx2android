"""The APK carries the corresponding source and license notices for GPL native code."""
from pathlib import Path
import tempfile
import unittest
import zipfile

from porter.source_archive import build_source_archive


class SourceArchive(unittest.TestCase):
    def test_archive_contains_native_host_and_license_sources(self):
        with tempfile.TemporaryDirectory(prefix="nx2a_source_") as tmp:
            archive = build_source_archive(ROOT, Path(tmp) / "source.zip", "0100000000010000")
            self.assertTrue(archive.is_file())
            self.assertLess(archive.stat().st_size, 30 * 1024 * 1024, 'source archive should not include unrelated game profiles or documentation artwork')
            with zipfile.ZipFile(archive) as z:
                names = set(z.namelist())
            for name in (
                "LICENSE",
                "NOTICE.md",
                "LICENSE-MIT-PORTER",
                "nxnative/LICENSE",
                "nxnative/LICENSE-MIT-ORIGINAL",
                "nxnative/dm/android/build.py",
                "nxnative/dm/android/dm_framegen.c",
                "nxnative/dm/android/lsfg/lsfg_chain.c",
                "nxnative/dm/android/lsfg/COPYING",
                "nxnative/dm/games/0100000000010000/android.tsv",
            ):
                self.assertIn(name, names)
            self.assertFalse(any(name.lower().endswith("/lossless.dll") for name in names))
            self.assertFalse(any(name.startswith("nxnative/dm/games/0100606026F12000/") for name in names))
            self.assertFalse(any(name.startswith("docs/images/") for name in names))


if __name__ == "__main__":
    unittest.main()
