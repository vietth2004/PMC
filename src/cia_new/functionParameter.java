package cia_new;

public class functionParameter
{
    private String paramName = "";
    private String paramType = "";

    public functionParameter()
    {

    }
    public functionParameter(String paramName, String paramType)
    {
        this.paramName = paramName;
        this.paramType = paramType;
    }

    public String getParamName()
    {
        return paramName;
    }

    public void setParamName(String paramName)
    {
        this.paramName = paramName;
    }

    public String getParamType()
    {
        return paramType;
    }

    public void setParamType(String paramType)
    {
        this.paramType = paramType;
    }
}
