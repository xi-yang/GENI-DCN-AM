#
# Convenience Makefile for building a custom plcapilib RPM
#
# Mark Huang <mlhuang@cs.princeton.edu>
# Copyright (C) 2005 The Trustees of Princeton University
#
# $Id: Makefile,v 1.2 2005/12/23 18:33:26 mlhuang Exp $
#

NAME := plcapilib
VERSION := 0.1
SOURCES := plcapilib.py plcsh setup.py
TMPDIRS := tmp build BUILD RPMS SOURCES SRPMS

# Trick rpmbuild to read .rpmmacros in the current directory
export HOME := $(shell pwd)

all: rpm

.rpmmacros:
	echo "%_topdir $(HOME)" > $@
	echo "%_tmppath $(HOME)/tmp" >> $@

SOURCES/$(NAME)-$(VERSION): $(SOURCES)
	find $(SOURCES) | cpio -p -d -u $@

SOURCES/$(NAME)-$(VERSION).tar.gz: SOURCES/$(NAME)-$(VERSION)
	tar --exclude CVS -C SOURCES -cpzf $@ $(notdir $<)

$(TMPDIRS):
	mkdir -p $@

rpm: $(NAME).spec .rpmmacros SOURCES/$(NAME)-$(VERSION).tar.gz $(TMPDIRS)
	rpmbuild $(RPMFLAGS) -ba $<

clean:
	rm -f *.pyc
	rm -f .rpmmacros
	rm -rf $(TMPDIRS)

force:

.PHONY: all clean rpm force
