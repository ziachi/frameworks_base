# frameworks_base — Matrixx 15 (Santoni)

Fork of [ProjectMatrixx/frameworks_base](https://github.com/ProjectMatrixx/frameworks_base) branch `15.0` with custom patches for **Xiaomi Redmi 4X (santoni)**.

## Device Info
| Item | Detail |
|------|--------|
| Device | Xiaomi Redmi 4X (santoni) |
| SoC | Qualcomm MSM8937 |
| RAM | 2GB |
| ROM | ProjectMatrixx v11.9.0 (Android 15) |
| Build Type | UNOFFICIAL |
| Maintainer | @kalomakan / @ziachi |

## Patch List

### [v7] #31 — Spectrum QS tile for kernel profile switching
- Added Quick Settings tile for kernel profile switching (Battery / Balance / Performance / Gaming)
- Tile reads/sets property `persist.spectrum.profile` and `spectrum.support`
- File: `packages/SystemUI/src/com/android/systemui/qs/tiles/SpectrumTile.java`

### [v7] #32 — SpectrumTile su -c setprop fix
- AOSP neverallow `property.te:482` blocks `platform_app` from setting vendor properties
- Fix: replaced `SystemProperties.set()` → `Runtime.exec("su -c setprop ...")` via helper `setSpectrumProfile()`
- `SystemProperties.get()` still used for reading (not affected by neverallow)
- File: `packages/SystemUI/src/com/android/systemui/qs/tiles/SpectrumTile.java`

### [v7] #33 — Missing API stubs for build compatibility
- Added missing method stubs required by other framework components during compilation:
  - `TelephonyPermissions.isShell(int uid)`
  - `TelephonyManager.getModemService()`
  - `PackageManager.resolveActivityAsUser(Intent, int, int, int)`
- File: `core/java/android/content/pm/PackageManager.java`, `telephony/common/com/android/internal/telephony/TelephonyPermissions.java`, `telephony/java/android/telephony/TelephonyManager.java`

## Usage

Add to `.repo/local_manifests/santoni.xml`:
```xml
<remove-project name="ProjectMatrixx/frameworks_base" />
<project path="frameworks/base" name="frameworks_base" remote="ziachi" revision="15.0" />
```

## Related Repos
- [device_xiaomi_santoni](https://github.com/ziachi/device_xiaomi_santoni) — Device tree
- [vendor_xiaomi_santoni](https://github.com/ziachi/vendor_xiaomi_santoni) — Vendor blobs
- [kernel_xiaomi_msm8937](https://github.com/ziachi/kernel_xiaomi_msm8937) — Kernel source

## Thanks To
- [ProjectMatrixx](https://github.com/ProjectMatrixx) — Base ROM
- [LineageOS](https://github.com/LineageOS) — Android base
- [nicholaschum/Flavor](https://github.com/nicholaschum/Flavor) — Spectrum kernel manager inspiration
