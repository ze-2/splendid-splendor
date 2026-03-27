// use enum so that it is not prone to mispelling 

public enum GemType {

    //enum constant declaration with arguments

    //  am passing these colour parameters into the enum constructor to initialise the colour variables
    RUBY("Red"), EMERALD("Green"), SAPPHIRE("Blue"), DIAMOND("White"), ONYX("Black"), GOLD("Yellow");

    private String colour;

    // enum constructors cannot be public 
    GemType(String colour) {
        this.colour = colour;
    }

    public String toString () {
        return String.format("[" + this.name() + ", " + this.colour + "]");
    }

}
