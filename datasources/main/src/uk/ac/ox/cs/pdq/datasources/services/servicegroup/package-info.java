// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.services.servicegroup;

// This package contains the objects used to parse in JAXB the Xml file service-groups.xml. It includes
// ServiceGroup as a whole which contains AttributeEncoding, GroupUsagePolicy and Service.
// AttributeEncoding is of type path-element or url-param depending on whether encoding of the GET Url itself
// is required or name/value pairs with ? and & encoding.
//
// Mark Ridler