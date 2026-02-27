#!/usr/bin/env bash
sbt clean compile coverage Test/test A11yTest/test it/test coverageReport dependencyUpdates