#!/bin/bash

jupyter-nbconvert make-book-latex.ipynb --to script --stdout | python
