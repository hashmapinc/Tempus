from setuptools import setup

setup(
    name='Tempus Cloud Documentation',
    version='1.0',
    description='Documentation for Tempus Cloud, an IIoT framework from Hashmap, Inc',
    author='Hashmap, Inc',
    author_email='Hashmap, Inc',
    packages=['Tempus Cloud Documentation'],  #same as name
    install_requires=['sphinx','sphinx-autobuild','sphinx_tabs', 'sphinx_rtd_theme'], #external packages as dependencies
)