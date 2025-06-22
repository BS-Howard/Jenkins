# Build Stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY . .
RUN dotnet restore net-example/JenkinsDemo/JenkinsDemo.sln
RUN dotnet publish net-example/JenkinsDemo/JenkinsDemo/JenkinsDemo.csproj -c Release -o /app/publish

# Runtime Stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
COPY --from=build /app/publish .

# 設定 ASP.NET Core 監聽環境變數改成 80 端口
ENV ASPNETCORE_URLS=http://+:80

# 開放容器內 80 端口
EXPOSE 80

ENTRYPOINT ["dotnet", "JenkinsDemo.dll"]