package authz

import rego.v1

# Default deny
default allow := false

# Allow if user has admin role
allow if {
	input.user.role == "admin"
}

# Allow if user owns the resource
allow if {
	input.user.id == input.resource.owner_id
}

# Allow read operations for authenticated users
allow if {
	input.action == "read"
	input.user.authenticated == true
}

# Deny if user is in blocklist
deny if {
	input.user.id in data.blocklist
}

# Final decision
decision := {"allow": allow, "deny": deny, "reason": reason} if {
	reason := "Access granted" if allow
	reason := "Access denied" if not allow
}
