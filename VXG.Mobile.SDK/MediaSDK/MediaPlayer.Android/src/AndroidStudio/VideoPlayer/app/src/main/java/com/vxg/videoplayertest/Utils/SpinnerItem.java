package com.vxg.videoplayertest.Utils;

public class SpinnerItem
{
    public int value = 0;
    public String text ;

    public SpinnerItem(){}
    {
        value = 0;
        text  = "";

    }
    public SpinnerItem(String text , int value)
    {
        this.value = value;
        this.text  = text;
    }
    public String toString()
    {
        return this.text;
    }

    public boolean equals(Object obj)
    {
        SpinnerItem other = (SpinnerItem) obj;
        return  (other.value == this.value) ? true : false ;
    }
} 
