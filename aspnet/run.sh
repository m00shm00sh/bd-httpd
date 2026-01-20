#!/bin/sh
dotnet build && DOTNET_ENVIRONMENT=Development dotnet bin/Debug/net9.0/aspnet.dll
