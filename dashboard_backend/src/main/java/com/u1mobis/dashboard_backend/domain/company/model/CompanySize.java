package com.u1mobis.dashboard_backend.domain.company.model;

public enum CompanySize {
    STARTUP("스타트업", 1, 10),
    SMALL("중소기업", 11, 50),
    MEDIUM("중견기업", 51, 300),
    LARGE("대기업", 301, Integer.MAX_VALUE);

    private final String displayName;
    private final int minEmployees;
    private final int maxEmployees;

    CompanySize(String displayName, int minEmployees, int maxEmployees) {
        this.displayName = displayName;
        this.minEmployees = minEmployees;
        this.maxEmployees = maxEmployees;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinEmployees() {
        return minEmployees;
    }

    public int getMaxEmployees() {
        return maxEmployees;
    }

    public boolean isValidEmployeeCount(int employeeCount) {
        return employeeCount >= minEmployees && employeeCount <= maxEmployees;
    }

    public static CompanySize fromEmployeeCount(int employeeCount) {
        for (CompanySize size : values()) {
            if (size.isValidEmployeeCount(employeeCount)) {
                return size;
            }
        }
        throw new IllegalArgumentException("Invalid employee count: " + employeeCount);
    }
}