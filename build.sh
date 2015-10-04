#!/bin/sh

mvn install
mkdir -p target/lib
cp target/*.jar target/lib

