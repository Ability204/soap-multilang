// npm install soap @vitalets/google-translate-api
// node clisoap2.js
// http://localhost:3000/?n=10
const http = require('http');
const soap = require('soap');
const { translate } = require('@vitalets/google-translate-api');

const wsdl = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL';

http.createServer(async (req, res) => {
  const { searchParams } = new URL(req.url, 'http://localhost:3000');
  const n = searchParams.get('n') || '10';
  const client = await soap.createClientAsync(wsdl);
  const [result] = await client.NumberToWordsAsync({ ubiNum: n });
  const { text } = await translate(result.NumberToWordsResult, { to: 'es' });
  res.setHeader('Content-Type', 'text/plain; charset=utf-8');
  res.end(text);
}).listen(3000, () => console.log('http://localhost:3000/?n=10'));
