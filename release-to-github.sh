#!/bin/sh

rm -fr build/dist/wasmJs/productionExecutable
rm -fr ../neojou.github.io/mandala/sandpile/*
 ./gradlew wasmJsBrowserDistribution
cp -R build/dist/wasmJs/productionExecutable/. ../neojou.github.io/mandala/sandpile/


