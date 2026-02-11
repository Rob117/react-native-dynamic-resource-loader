# react-native-dynamic-resource-loader

Load iOS [On-Demand Resources](https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/On_Demand_Resources_Guide/) from React Native. Download tagged assets from the App Store at runtime to reduce your initial app size.

> **iOS only.** Android methods are stubs (promises reject, sync methods no-op).

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

Tell iOS it can purge these resources when storage is low:

```js
endAccessingResources(['level1']);
```

### Set preservation priority

Control which resources iOS purges first (0.0 = purge first, 1.0 = keep longest):

```js
setPreservationPriority(0.8, ['level1']);
```

## API

| Method | Returns | Description |
|--------|---------|-------------|
| `downloadResources(tags)` | `Promise<boolean>` | Download resources for the given tags. Checks cache first, falls back to App Store download. |
| `checkResourcesAvailable(tags)` | `Promise<boolean>` | Check if tagged resources are already on device without downloading. |
| `endAccessingResources(tags)` | `void` | Release resources so iOS can purge them when storage is low. |
| `getResourcePath(name, type)` | `Promise<string>` | Get the absolute file path for a resource after download. |
| `setPreservationPriority(priority, tags)` | `void` | Set purge priority (0.0-1.0). Higher values are kept longer. |

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
