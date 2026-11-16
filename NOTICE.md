# NOTICE

nx2android — per-game Android APK porting tool

Copyright (C) 2026 Ragnarok93 for modifications made in this fork.
Copyright (C) 2026 seedds for the upstream native engine tree.
Copyright (C) 2026 shirai91 for the upstream desktop porter and GUI.
Copyright (C) 2026 Eden Emulator Project and lsfg-vk contributors for the LSFG frame-generation implementation and its upstream components.

This combined program is distributed under the GNU General Public License, version 3
or (at your option) any later version. See [LICENSE](LICENSE).

## Scope and retained notices

The repository's own maintained code, including the per-game APK porter, GUI,
Android integration and native host, is distributed as part of this GPL-3.0-or-later
combined work. The original MIT-licensed upstream code is sublicensed under GPL-3.0-or-later
under the MIT license's express sublicense grant. Its original copyright and permission
notice are preserved in [nxnative/LICENSE-MIT-ORIGINAL](nxnative/LICENSE-MIT-ORIGINAL)
and reproduced below. The original notice remains applicable to copies of those
upstream components distributed under the MIT grant.

### Original MIT license notice

MIT License

Copyright (c) 2026 seedds

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## LSFG frame generation

The LSFG C port in `nxnative/dm/android/lsfg/` is GPL-3.0-or-later and retains
its Eden Emulator Project and lsfg-vk SPDX copyright notices. Its full license
and provenance notes are in
[nxnative/dm/android/lsfg/LICENSE.md](nxnative/dm/android/lsfg/LICENSE.md);
the full GPL text is in [nxnative/dm/android/lsfg/COPYING](nxnative/dm/android/lsfg/COPYING).

**Lossless.dll is proprietary and is not included in this repository or in any APK.**
Frame generation reads shaders at runtime from the player's own copy of the DLL,
which the player imports through Android's document picker.

## Third-party components

Third-party code under `nxnative/dm/third_party/` retains the license and notices
in its own directories. Those notices are not replaced by this repository-level
license.

## Game files and trademarks

This repository and its APK build workflow contain no game dumps, game files,
console keys or firmware. The user supplies their own legally obtained game data.
Nintendo Switch, game names and other trademarks identify compatibility only;
this project is not affiliated with or endorsed by Nintendo or the relevant publishers.
