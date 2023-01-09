/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.wallet.utils;

/**
 *
 * @author francesco.pasetto@h2tcoin.com
 */
public class AnsiColor {

    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static final String BRIGHT_BLACK = "\u001B[30;1m";
    public static final String BRIGHT_RED = "\u001B[31;1m";
    public static final String BRIGHT_GREEN = "\u001B[32;1m";
    public static final String BRIGHT_YELLOW = "\u001B[33;1m";
    public static final String BRIGHT_BLUE = "\u001B[34;1m";
    public static final String BRIGHT_PURPLE = "\u001B[35;1m";
    public static final String BRIGHT_CYAN = "\u001B[36;1m";
    public static final String BRIGHT_WHITE = "\u001B[37;1m";

    public static final String RED_BG = "\u001B[41m";
    public static final String BLACK_BG = "\u001B[40m";
    public static final String GREEN_BG = "\u001B[42m";
    public static final String YELLOW_BG = "\u001B[43m";
    public static final String BLUE_BG = "\u001B[44m";
    public static final String PURPLE_BG = "\u001B[45m";
    public static final String CYAN_BG = "\u001B[46m";
    public static final String WHITE_BG = "\u001B[47m";

    public static final String BRIGHT_RED_BG = "\u001B[41;1m";
    public static final String BRIGHT_BLACK_BG = "\u001B[40;1m";
    public static final String BRIGHT_GREEN_BG = "\u001B[42;1m";
    public static final String BRIGHT_YELLOW_BG = "\u001B[43:1m";
    public static final String BRIGHT_BLUE_BG = "\u001B[44;1m";
    public static final String BRIGHT_PURPLE_BG = "\u001B[45;1m";
    public static final String BRIGHT_CYAN_BG = "\u001B[46;1m";
    public static final String BRIGHT_WHITE_BG = "\u001B[47;1m";

    public static void printLogo() {
        System.out.println(AnsiColor.WHITE + "                  ______________" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                 |              |" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                 |  ---  .----  |" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                 |____  /  _____|" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                       /  / ." + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                      |  |  |" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                      |  |  |" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "                      |_____|" + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + " _______    _                         _         " + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + "|__   __|  | |                       | |        " + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + "   | | __ _| | ____ _ _ __ ___   __ _| | ____ _ " + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + "   | |/ _` | |/ / _` | '_ ` _ \\ / _` | |/ / _` |" + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + "   | | (_| |   < (_| | | | | | | (_| |   < (_| |" + AnsiColor.RESET);
        System.out.println(AnsiColor.BRIGHT_BLUE + "   |_|\\__,_|_|\\_\\__,_|_| |_| |_|\\__,_|_|\\_\\__,_|" + AnsiColor.RESET);
        System.out.println(AnsiColor.WHITE + "     E n t e r p r i s e   B l o c k C h a i n" + AnsiColor.RESET);
    }
    
    public static final String EMOJI_LOCKED="\uD83D\uDD12";
    public static final String EMOJI_UNLOCKED="\uD83D\uDD13";
    public static final String EMOJI_KEY="\uD83D\uDD11";

}
