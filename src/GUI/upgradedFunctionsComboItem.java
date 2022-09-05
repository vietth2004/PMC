package GUI;

public class upgradedFunctionsComboItem
{
    private String key;
    private upgradedFunctions value;

    public upgradedFunctionsComboItem(String key, upgradedFunctions value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return key;
    }

    public String getKey()
    {
        return key;
    }

    public upgradedFunctions getValue()
    {
        return value;
    }
}
