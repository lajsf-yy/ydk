const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');
const workerPath = path.join(__dirname, 'codegen_worker.js');
const subprocess = spawn(process.argv[0], [workerPath, process.argv[2]], {
  detached: true,
  stdio: 'ignore',
});

subprocess.unref();
