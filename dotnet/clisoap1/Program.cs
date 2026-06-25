// dotnet run
// http://localhost:5000/?n=10
using System.Xml.Linq;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", async (int n) =>
{
    const string endpoint =
        "https://www.dataaccess.com/webservicesserver/NumberConversion.wso";

    var envelope = $"""
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
              <ubiNum>{n}</ubiNum>
            </NumberToWords>
          </soap:Body>
        </soap:Envelope>
        """;

    using var http = new HttpClient();
    var content = new StringContent(envelope, System.Text.Encoding.UTF8, "text/xml");
    var response = await http.PostAsync(endpoint, content);
    var xml = XDocument.Parse(await response.Content.ReadAsStringAsync());
    XNamespace ns = "http://www.dataaccess.com/webservicesserver/";
    return xml.Descendants(ns + "NumberToWordsResult").First().Value;
});

app.Run("http://localhost:5000");
