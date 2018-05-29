package uk.ac.ox.cs.pdq.datasources.services.policies;

// The UsagePolicies are as-defined in the former implementation, repurposed in the new implementation.
// They represent the various ways that a limitation can impose itself with regards to the use of a service.
// For example, PeriodicalAllowance is a generic implementation which covers RequestAllowance, ResultAllowance
// and DataDownloadAllowance.
//
// UsagePolicies implement AccessPreProcessor or AccessPostProcessor or both. These are the hook-in points
// where the code asserts itself as a usage violation in the event that one of the limits is triggered.
//
// Mark Ridler