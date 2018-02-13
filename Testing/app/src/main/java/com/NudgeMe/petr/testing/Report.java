package com.NudgeMe.petr.testing;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Petr on 02.10.2017.
 */

@IgnoreExtraProperties
public class Report
{

    public long Y;
    public String reportedText;
    public int sendValue;

    public Report()
    {

    }
    public Report(long Y, String reportedText, int sendValue)
    {
        this.Y = Y;
        this.reportedText = reportedText;
        this.sendValue = sendValue;
    }

}
