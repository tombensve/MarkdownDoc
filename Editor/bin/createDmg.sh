#!/usr/bin/env bash

cd  ../target
hdiutil create -srcfolder MarkdownDocEditor-1.4/MarkdownDocEditor.app MarkdownDocEditor-1.4.dmg
