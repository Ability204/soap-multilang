// dotnet add package Humanizer.Core.es
// dotnet run
// http://localhost:5000/?n=10
using Humanizer;
using System.Globalization;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapGet("/", (int n) => n.ToWords(new CultureInfo("es")));

app.Run("http://localhost:5000");
