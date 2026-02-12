# react-native-dynamic-resource-loader

Load iOS [On-Demand Resources](https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/On_Demand_Resources_Guide/) and Android [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery) from React Native. Download tagged assets at runtime to reduce your initial app size.

## Installation

```sh
npm install react-native-dynamic-resource-loader
```

## Xcode Setup

Before using the library, you need to configure On-Demand Resources in your Xcode project:

1. **Enable ODR** — In your Xcode project, go to Build Settings and set `ENABLE_ON_DEMAND_RESOURCES = YES`.

2. **Add resources and assign tags** — Add files (images, data files, etc.) to your Xcode project's Resources build phase. Select each file in Xcode, open the File Inspector, and add one or more tags under "On Demand Resource Tags".

3. **Register tags** — Xcode does this automatically when you assign tags through the UI. You'll see them listed in your project's attributes under `KnownAssetTags`.

4. **Debug builds** — When running from the command line (e.g. `yarn ios`), Xcode's local asset server isn't available. Set `EMBED_ASSET_PACKS_IN_PRODUCT_BUNDLE = YES` in your Debug build settings so ODR assets are embedded directly in the app bundle during development.

Multiple resources can share the same tag. When you download a tag, iOS fetches all resources with that tag together.

## Android Setup

Android uses [Play Asset Delivery](https://developer.android.com/guide/playcore/asset-delivery) (PAD) to deliver assets on demand. Each iOS "tag" maps to an Android "asset pack".

### 1. Add the PAD dependency

In your app's `android/app/build.gradle`:

```groovy
dependencies {
    implementation("com.google.android.play:asset-delivery:2.3.0")
}
```

### 2. Create an asset pack module

For each tag (e.g. `kichilogo`), create a directory at `android/kichilogo/` with this structure:

```
android/kichilogo/
├── build.gradle
└── src/main/assets/
    └── your_file.png
```

`android/kichilogo/build.gradle`:
```groovy
apply plugin: 'com.android.asset-pack'

assetPack {
    packName = "kichilogo"
    dynamicDelivery {
        deliveryType = "on-demand"
    }
}
```

### 3. Wire up Gradle

In `android/settings.gradle`, add:
```groovy
include ':kichilogo'
```

In `android/app/build.gradle`, inside the `android {}` block, add:
```groovy
assetPacks = [":kichilogo"]
```

### 4. Local testing

On-demand asset packs are **not included** in a regular `./gradlew installDebug` APK. There is no Android equivalent to iOS's `EMBED_ASSET_PACKS_IN_PRODUCT_BUNDLE`. To test locally, use `bundletool` with the `--local-testing` flag, which tells the Play Core library to serve on-demand packs from the device's local storage instead of the Play Store.

Install `bundletool`:
```bash
brew install bundletool
```

Build and install:
```bash
cd android

# 1. Build an AAB (Android App Bundle)
./gradlew bundleDebug

# 2. Convert to APKs with --local-testing
bundletool build-apks \
  --bundle=app/build/outputs/bundle/debug/app-debug.aab \
  --output=app.apks \
  --local-testing

# 3. Install on device/emulator
bundletool install-apks --apks=app.apks
```

The `AssetPackManager` API works normally — `fetch()` will "download" instantly from the local copy.

> **Note:** You need to re-run these steps whenever you change assets or native code. JS-only changes still work with Metro hot reload after the initial install.

## Usage

```js
import {
  checkResourcesAvailable,
  downloadResources,
  endAccessingResources,
  getResourcePath,
  setPreservationPriority,
} from 'react-native-dynamic-resource-loader';
```

### Download resources and get file paths

```js
// Download all resources tagged "level1"
const success = await downloadResources(['level1']);

// Look up individual files by name (you know which files you tagged)
const spritePath = await getResourcePath('enemy_sprite', 'png');
const mapPath = await getResourcePath('level_map', 'json');
```

### Check if resources are already cached

```js
const available = await checkResourcesAvailable(['level1']);
if (!available) {
  await downloadResources(['level1']);
}
```

### Release resources when done

Tell the OS it can purge these resources when storage is low (on Android, removes the asset pack):

```js
endAccessingResources(['level1']);
```

### Set preservation priority

Control which resources iOS purges first (0.0 = purge first, 1.0 = keep longest). No-op on Android:

```js
setPreservationPriority(0.8, ['level1']);
```

## API

| Method | Returns | Description |
|--------|---------|-------------|
| `downloadResources(tags)` | `Promise<boolean>` | Download resources for the given tags. Checks cache first. |
| `checkResourcesAvailable(tags)` | `Promise<boolean>` | Check if tagged resources are already on device without downloading. |
| `endAccessingResources(tags)` | `void` | Release resources so the OS can purge them (iOS) or remove the pack (Android). |
| `getResourcePath(name, type)` | `Promise<string>` | Get the absolute file path for a resource after download. |
| `setPreservationPriority(priority, tags)` | `void` | Set purge priority (0.0-1.0). iOS only; no-op on Android. |

## Full Example

```js
import { useEffect, useState } from 'react';
import {
  downloadResources,
  getResourcePath,
  endAccessingResources,
} from 'react-native-dynamic-resource-loader';

function MyComponent() {
  const [imagePath, setImagePath] = useState(null);

  useEffect(() => {
    let mounted = true;

    downloadResources(['avatars'])
      .then(() => getResourcePath('profile_pic', 'png'))
      .then((path) => {
        if (mounted) setImagePath(path);
      })
      .catch((e) => console.log('ODR failed:', e.message));

    return () => {
      mounted = false;
      endAccessingResources(['avatars']);
    };
  }, []);

  return imagePath ? <Image source={{ uri: `file://${imagePath}` }} /> : null;
}
```

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
