#!/usr/bin/python
#
# Setup script for the plcapilib library
#
# Mark Huang <mlhuang@cs.princeton.edu>
# Copyright (C) 2005 The Trustees of Princeton University
#
# $Id: setup.py,v 1.1 2005/12/22 21:43:25 mlhuang Exp $
#

from distutils.core import setup

setup(py_modules=["plcapilib"], scripts=["plcsh"])

