# Getting Started
Tempus uses Sphinx to manage its documentation. The actual format of the documentation is [ReStructured Text](http://docutils.sourceforge.net/docs/user/rst/quickref.html)

## Pre-requisites
You need at least Python 2.7 to get started

## Installation
If this is your first time using these docs, install dependencies from the `requirements.txt` with pip using the following command:

```bash
pip install -r ./requirements.txt
```

## Building
In order to make the HTML version, from the root of the /docs folder, execute: 
```bash
make html
```

To make html continuously while developing documentation, execute:
```bash
make auto
```
