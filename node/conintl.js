// npm install written-number
// node conintl.js
// http://localhost:3000/?n=10
const http = require('http');
const writtenNumber = require('written-number');

writtenNumber.defaults.lang = 'es';

http.createServer((req, res) => {
  const { searchParams } = new URL(req.url, 'http://localhost:3000');
  const n = parseInt(searchParams.get('n') || '10');
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.end(writtenNumber(n));
}).listen(3000, () => console.log('http://localhost:3000/?n=10'));
