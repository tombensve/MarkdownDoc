#!/usr/bin/env bash

cd  ../target
hdiutil create -srcfolder MarkdownDocEditor-1.4.1/MarkdownDocEditor.app MarkdownDocEditor-1.4.1.dmg
