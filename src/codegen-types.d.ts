// Type declarations for react-native codegen types that are blocked
// by the react-native-strict-api custom condition in tsconfig.json.
declare module 'react-native/Libraries/Types/CodegenTypes' {
  import type { EventSubscription } from 'react-native';

  export type EventEmitter<T> = (
    handler: (event: T) => void | Promise<void>
  ) => EventSubscription;
}
