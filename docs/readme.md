# Getting Started


Tempus uses Sphinx to manage its documentation. The acutal format of the documentation is [ReStructured Text](http://docutils.sourceforge.net/docs/user/rst/quickref.html)
## Pre-requisites

You need at least Python 2.7 to get started

## Sphinx installation

To get Sphinx use pip:

```bash
pip install sphinx sphinx-autobuild
```

The HTML output also makes use of [sphinx-tabs](https://github.com/djungelorm/sphinx-tabs). To install this execute:

```bash
pip install sphinx_tabs
```

We are also using sphinx_rtd_theme hence to install that execute: 


```bash
pip install sphinx_rtd_theme
```

In order to make the HTML version, from the root of the /docs folder, execute: 

```bash
make html
```
