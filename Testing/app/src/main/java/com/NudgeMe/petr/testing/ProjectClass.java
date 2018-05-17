package com.NudgeMe.petr.testing;

import java.util.ArrayList;

/**
 * Created by Petr on 14.10.2017.
 */


// Class for project structure used in ProjectsActivity
public class ProjectClass {

    private String projectName;
    private String id;
    private ArrayList<String> projectUsers = new ArrayList<>();

    public ProjectClass()
    {

    }

    public String getProjectName()
    {
        return  projectName;
    }
    public String getID()
    {
        return  id;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public void setId(String id) {
        this.id = id;
    }

}
