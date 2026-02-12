import { useEffect, useState } from 'react';
import { Text, Image, StyleSheet, ScrollView, Platform } from 'react-native';
import {
  downloadResources,
  getResourcePath,
} from 'react-native-dynamic-resource-loader';

const TAGS = ['kichilogo'];

export default function App() {
  const [status, setStatus] = useState('Downloading...');
  const [imagePath, setImagePath] = useState<string | null>(null);

  useEffect(() => {
    downloadResources(TAGS)
      .then(() => getResourcePath('kichi512', 'png'))
      .then((path) => {
        console.log('downloadResources succeeded, path:', path);
        setStatus(`Success\n${path}`);
        setImagePath(path);
      })
      .catch((e: any) => {
        console.log('downloadResources failed:', e.message);
        setStatus(`Failed: ${e.message}`);
      });
  }, []);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>Dynamic Resource Loader</Text>
      <Text style={styles.platform}>Platform: {Platform.OS}</Text>
      <Text style={styles.status}>{status}</Text>
      {imagePath && (
        <Image source={{ uri: `file://${imagePath}` }} style={styles.image} />
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    padding: 24,
    paddingTop: 60,
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  platform: {
    fontSize: 14,
    color: '#666',
    marginBottom: 20,
  },
  status: {
    fontSize: 16,
    fontFamily: Platform.OS === 'ios' ? 'Menlo' : 'monospace',
  },
  image: {
    width: 256,
    height: 256,
    marginTop: 20,
  },
});
