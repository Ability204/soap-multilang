// npm install soap
// node clisoap1.js
// http://localhost:3000/?n=10
const http = require('http');
const soap = require('soap');

const wsdl = 'https://www.dataaccess.com/webservicesserver/NumberConversion.wso?WSDL';

http.createServer(async (req, res) => {
  const { searchParams } = new URL(req.url, 'http://localhost:3000');
  const n = searchParams.get('n') || '10';
  const client = await soap.createClientAsync(wsdl);
  const [result] = await client.NumberToWordsAsync({ ubiNum: n });
  res.end(result.NumberToWordsResult);
}).listen(3000, () => console.log('http://localhost:3000/?n=10'));
