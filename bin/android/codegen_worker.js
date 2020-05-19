const http = require('http');
const fs = require('fs');
const filewatcher = null;
const server = http.createServer((req, res) => {
  const ip = res.socket.remoteAddress;
  const port = res.socket.remotePort;
  if (req.url.indexOf('stop') > -1) {
    res.end(`stop  watching `);
    filewatcher && filewatcher.close();
    server.close();
  } else {
    res.end(`watching android ${process.argv[2]}`);
  }
});
server.listen(55210);
const androidRoot = process.argv[2];
console.warn('start watch file', androidRoot);

fs.watch(androidRoot, (event, filename) => {
  console.warn('file change', filename);
  // filename.endsWith('.java') &&
});
// let androidRoot = process.argv[2];
// let settingsgradle = fs.readFileSync(path.join(androidRoot, 'settings.gradle')).toString();
// settingsgradle += '\n //test write';
// fs.writeFileSync(path.join(androidRoot, 'settings.gradle'), settingsgradle);
