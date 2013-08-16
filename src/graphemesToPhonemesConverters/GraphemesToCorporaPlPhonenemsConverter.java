package graphemesToPhonemesConverters;

public class GraphemesToCorporaPlPhonenemsConverter extends NaiveGraphemesToPhonemesConverter
{
    private final static String[][][] polishGraphemes = new String[][][]{
        {{"a"}, {"a"}},
        {{"ą"}, {"o ll"}},
        {{"b"}, {"b", "p"}},
        {{"c"}, {"c"}},
        {{"ci"}, {"ci"}},
        {{"ć"}, {"ci"}},
        {{"cz"}, {"cz"}},
        {{"d"}, {"d"}},
        {{"dz"}, {"dz"}},
        {{"dzi"}, {"d zi"}},
        {{"dż"}, {"drz"}},
        {{"dź"}, {"d zi"}},
        {{"e"}, {"e"}},
        {{"ę"}, {"e ll"}},
        {{"f"}, {"f"}},
        {{"g"}, {"g"}},
        {{"gi"}, {"gi"}},
        {{"h"}, {"h"}},
        {{"ch"}, {"h"}},
        {{"chrz"}, {"h sz"}},
        {{"chż"}, {"h sz"}},
        {{"i"}, {"i"}},
        {{"j"}, {"j"}},
        {{"k"}, {"k"}},
        {{"ki"}, {"ki"}},
        {{"krz"}, {"k sz"}},
        {{"kż"}, {"k sz"}},
        {{"l"}, {"l"}},
        {{"ł"}, {"ll"}},
        {{"m"}, {"m"}},
        {{"n"}, {"n"}},
        {{"ni"}, {"ni"}},
        {{"ń"}, {"ni"}},
        {{"o"}, {"o"}},
        {{"ó"}, {"u"}},
        {{"p"}, {"p"}},
        {{"r"}, {"r"}},
        {{"rz"}, {"rz"}},
        {{"s"}, {"s"}},
        {{"si"}, {"si"}},
        {{"sz"}, {"sz"}},
        {{"ś"}, {"si"}},
        {{"t"}, {"t"}},
        {{"trz"}, {"t sz"}},
        {{"tż"}, {"t sz"}},
        {{"u"}, {"u"}},
        {{"v"}, {"w", "f"}},
        {{"w"}, {"w", "f"}},
        {{"y"}, {"y"}},
        {{"z"}, {"z", "s"}},
        {{"zi"}, {"zi"}},
        {{"ż"}, {"rz"}},
        {{"ź"}, {"zi"}},
        {{"ü"}, {"u"}}
    };

    public GraphemesToCorporaPlPhonenemsConverter()
    {
        super(polishGraphemes);
    }
}
