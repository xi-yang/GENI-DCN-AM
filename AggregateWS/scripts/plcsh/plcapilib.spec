#
# $Id: plcapilib.spec 7668 2008-01-08 11:49:43Z thierry $
#
%define url $URL: http://svn.planet-lab.org/svn/plcmdline/trunk/plcapilib.spec $

%define name plcapilib
%define version 0.1
%define taglevel 5

%define release %{taglevel}%{?pldistro:.%{pldistro}}%{?date:.%{date}}

Summary: PlanetLab Central API Library
Name: %{name}
Version: %{version}
Release: %{release}
License: GPL
Group: Development/Languages
Source0: %{name}-%{version}.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root

Vendor: PlanetLab
Packager: PlanetLab Central <support@planet-lab.org>
Distribution: PlanetLab %{plrelease}
URL: %(echo %{url} | cut -d ' ' -f 2)

%description
plcapilib is a wrapper around xmlrpclib for interfacing with PLCAPI
servers. plcsh is an interactive Python shell that uses plcapilib to
provide an easy-to-use environment for executing PLCAPI methods.

%prep
%setup -q

%build

%install
rm -rf $RPM_BUILD_ROOT
install -D -m 755 plcsh $RPM_BUILD_ROOT/%{_bindir}/plcsh
install -D -m 644 plcapilib.py $RPM_BUILD_ROOT/%{_datadir}/%{name}/plcapilib.py

%clean
rm -rf $RPM_BUILD_ROOT

%post
# Byte compile and install
pushd %{_datadir}/%{name} >/dev/null
%{__python} plcapilib.py build
%{__python} plcapilib.py install
rm -rf build
popd

%triggerpostun -- %{name}
# RPMs get upgraded by installing the new one, then uninstalling the
# old one. Since we no longer own the byte-compiled modules, they may
# be removed right after we create them in %post if we are upgraded
# from a version that did own them at one point. This section should
# be removed once all packages have been upgraded to at least this
# version.

# Byte compile and install
pushd %{_datadir}/%{name} >/dev/null
%{__python} plcapilib.py build
%{__python} plcapilib.py install
rm -rf build
popd

%preun
# 0 = erase, 1 = upgrade
if [ $1 -eq 0 ] ; then
    python_sitelib=$(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")
    rm -f "$python_sitelib"/plcapilib.py*
fi

%files
%defattr(-,root,root,-)
%doc
%{_bindir}/plcsh
%{_datadir}/%{name}


%changelog
* Thu Dec 22 2005 Mark Huang <mlhuang@paris.CS.Princeton.EDU> - 
- Initial build.

