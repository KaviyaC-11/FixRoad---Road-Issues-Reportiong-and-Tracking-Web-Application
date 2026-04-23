package com.fixroad.dto;

public class AssignRepairRequest {

    private String repairTeamName;
    private String repairContactNumber;

    public String getRepairTeamName() {
        return repairTeamName;
    }

    public void setRepairTeamName(String repairTeamName) {
        this.repairTeamName = repairTeamName;
    }

    public String getRepairContactNumber() {
        return repairContactNumber;
    }

    public void setRepairContactNumber(String repairContactNumber) {
        this.repairContactNumber = repairContactNumber;
    }
}