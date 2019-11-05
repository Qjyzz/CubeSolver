package com.isvsa;
/**
 * Created by ifcan on 2017/12/13.
 */

public class Cube1 {
    static String faces = "RLFBUD";
    static char[] order = "AECGBFDHIJKLMSNTROQP".toCharArray();
    static char[] bithash = "TdXhQaRbEFIJUZfijeYV".toCharArray();
    static char[] perm = "AIBJTMROCLDKSNQPEKFIMSPRGJHLNTOQAGCEMTNSBFDHORPQ".toCharArray();//以8个为一组分别代表FBRLUD面，以此来为旋转面上各位置的方向、所含块提供准确变化方式
    static char[] pos = new char[20];//各个位置所含的方块
    static char[] ori = new char[20];//各个位置所含方块的朝向
    static char[] val = new char[20];//各个位置所含方块朝向总数
    static char[][] tables = new char[8][];
    static int[] move = new int[20];
    static int[] moveamount = new int[20];
    static int phase = 0;
    static int[] tablesize = { 1, 4096, 6561, 4096, 256, 1536, 13824, 576 };//建表函数
    static int CHAROFFSET = 65;

    static private int Char2Num(char c)
    {
        return (int)c - CHAROFFSET;
    }

    static private void cycle(char[] p, char[] a, int offset)
    {
        char temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[1 + offset])];
        p[Char2Num(a[1 + offset])] = temp;
        temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[2 + offset])];
        p[Char2Num(a[2 + offset])] = temp;
        temp = p[Char2Num(a[0 + offset])];
        p[Char2Num(a[0 + offset])] = p[Char2Num(a[3 + offset])];
        p[Char2Num(a[3 + offset])] = temp;
    }

    static private void twist(int i, int a)
    {
        i -= CHAROFFSET;
        ori[i] = (char)(((int)ori[i] + a + 1) % val[i]);
    }

    static private void reset()
    {
        for (int i = 0; i < 20; pos[i] = (char)(i), ori[i++] = '\0') ;
    }//使魔方复原

    static private int permtonum(char[] p, int offset)
    {
        int n = 0;
        for (int a = 0; a < 4; a++)
        {
            n *= 4 - a;
            for (int b = a; ++b < 4; )
                if (p[b + offset] < p[a + offset]) n++;
        }
        return n;
    }

    static private void numtoperm(char[] p, int n, int o)
    {
        //p += o;
        p[3 + o] = (char)(o);
        for (int a = 3; a-- > 0; )
        {
            p[a + o] = (char)(n % (4 - a) + o);
            n /= 4 - a;
            for (int b = a; ++b < 4; )
                if (p[b + o] >= p[a + o]) p[b + o]++;
        }
    }

    static private int getposition(int t)
    {
        int i = -1, n = 0;
        switch (t)
        {
            case 1:
                for (; ++i < 12; ) n += ((int)ori[i]) << i;
                break;
            case 2:
                for (i = 20; --i > 11; ) n = n * 3 + (int)ori[i];
                break;
            case 3:
                for (; ++i < 12; ) n += ((((int)pos[i]) & 8) > 0) ? (1 << i) : 0;
                break;
            case 4:
                for (; ++i < 8; ) n += ((((int)pos[i]) & 4) > 0) ? (1 << i) : 0;
                break;
            case 5:
                int[] corn = new int[8];
                int[] corn2 = new int[4];
                int j, k, l;
                k = j = 0;
                for (; ++i < 8; )
                    if (((l = pos[i + 12] - 12) & 4) > 0)
                    {
                        corn[l] = k++;
                        n += 1 << i;
                    }
                    else corn[j++] = l;
                for (i = 0; i < 4; i++) corn2[i] = corn[4 + corn[i]];
                for (; --i > 0; ) corn2[i] ^= corn2[0];

                n = n * 6 + corn2[1] * 2 - 2;
                if (corn2[3] < corn2[2]) n++;
                break;
            case 6:
                n = permtonum(pos, 0) * 576 + permtonum(pos, 4) * 24 + permtonum(pos, 12);
                break;
            case 7:
                n = permtonum(pos, 8) * 24 + permtonum(pos, 16);
                break;

        }
        return n;
    }

    static private void setposition(int t, int n)
    {
        int i = 0, j = 12, k = 0;
        char[] corn = "QRSTQRTSQSRTQTRSQSTRQTSR".toCharArray();
        reset();
        switch (t)
        {
            // case 0 does nothing so leaves cube solved
            case 1://edgeflip
                for (; i < 12; i++, n >>= 1) ori[i] = (char)(n & 1);
                break;
            case 2://cornertwist
                for (i = 12; i < 20; i++, n /= 3) ori[i] = (char)(n % 3);
                break;
            case 3://middle edge choice
                for (; i < 12; i++, n >>= 1) pos[i] = (char)(8 * n & 8);
                break;
            case 4://ud slice choice
                for (; i < 8; i++, n >>= 1) pos[i] = (char)(4 * n & 4);
                break;
            case 5://tetrad choice,parity,twist
                int offset = n % 6 * 4;
                n /= 6;
                for (; i < 8; i++, n >>= 1)
                    pos[i + 12] = (char)(((n & 1) > 0) ? corn[offset + k++] - CHAROFFSET : j++);
                break;
            case 6://slice permutations
                numtoperm(pos, n % 24, 12); n /= 24;
                numtoperm(pos, n % 24, 4); n /= 24;
                numtoperm(pos, n, 0);
                break;
            case 7://corner permutations
                numtoperm(pos, n / 24, 8);
                numtoperm(pos, n % 24, 16);
                break;
        }
    }

    static private void domove(int m)//m从0~5分别代表FBRLUD面的旋转，顺时针
    {
        //char* p = perm + 8 * m;得到perm中该面的位置
        int offset = 8 * m;
        int i = 8;
        //cycle the edges
        cycle(pos, perm, offset);//旋转一面上四个棱块，即当前块得到前一块的方块值
        cycle(ori, perm, offset);//得到前一方块的朝向
        //cycle the corners
        cycle(pos, perm, offset + 4);//旋转一面上四个角块，即当前块得到前一块的方块值
        cycle(ori, perm, offset + 4);//得到前一方块的朝向
        //twist corners if RLFB
        if (m < 4)
            for (; --i > 3; ) twist(perm[i + offset], i & 1);//旋转角块朝向如果是RLFB面转动
        //flip edges if FB
        if (m < 2)
            for (i = 4; i-- > 0; ) twist(perm[i + offset], 0);//旋转角块朝向如果是FB面转动
    }

    static private void filltable(int ti)//产生一个表格供寻找得到解决途径
    {
        int n = 1, l = 1, tl = tablesize[ti];//规定表格大小
        char[] tb = new char[tl];
        tables[ti] = tb;
        for (int i = 0; i < tb.length; i++) tb[i] = '\0';

        reset();//回到复原状态
        tb[getposition(ti)] = (char)(1);//标记当前为第一层

        // while there are positions of depth l
        while (n > 0)//
        {
            n = 0;
            // find each position of depth l
            for (int i = 0; i < tl; i++)//遍历来找L层所有情况
            {
                if (tb[i] == l)
                {
                    //construct that cube position
                    setposition(ti, i);//使魔方到table[ti]的序列i情况
                    // try each face any amount
                    for (int f = 0; f < 6; f++)
                    {
                        for (int q = 1; q < 4; q++)
                        {
                            domove(f);
                            // get resulting position
                            int r = getposition(ti);//得到旋转后的序列
                            // if move as allowed in that phase, and position is a new one
                            if ((q == 2 || f >= (ti & 6)) && tb[r] == '\0')
                            {
                                // mark that position as depth l+1
                                tb[r] = (char)(l + 1);
                                n++;
                            }
                        }
                        domove(f);
                    }
                }
            }
            l++;
        }
    }

    static private boolean searchphase(int movesleft, int movesdone, int lastmove)
    {
        if (tables[phase][getposition(phase)] - 1 > movesleft ||
                tables[phase + 1][getposition(phase + 1)] - 1 > movesleft) return false;

        if (movesleft == 0) return true;

        for (int i = 6; i-- > 0; )
        {
            if ((i - lastmove != 0) && ((i - lastmove + 1) != 0 || ((i | 1) != 0)))
            {
                move[movesdone] = i;
                for (int j = 0; ++j < 4; )
                {
                    domove(i);
                    moveamount[movesdone] = j;
                    if ((j == 2 || i >= phase) &&
                            searchphase(movesleft - 1, movesdone + 1, i)) return true;
                }
                domove(i);
            }
        }
        return false;
    }

    static public String GetResult(String sInput)
    {
        //sInput="RU LF RD RF FU UL BD DF RB LB BU LD LBD URB LUB DRF ULF FLD RDB UFR";
        phase = 0;

        String[] argv = sInput.split(" ");
        String sOutput = "";
        if (argv.length != 20)	return "error";

        int f, i = 0, j = 0, k = 0, pc, mor;

        for (; k < 20; k++) val[k] = (char)(k < 12 ? 2 : 3);
        for (; j < 8; j++) filltable(j);

        for (; i < 20; i++)
        {
            f = pc = k = mor = 0;
            for (; f < val[i]; f++)
            {
                j = faces.indexOf(argv[i].charAt(f));
                if (j > k) { k = j; mor = f; }
                pc += 1 << j;
            }
            for (f = 0; f < 20; f++)
                if (pc == bithash[f] - 64) break;

            pos[order[i] - CHAROFFSET] = (char)(f);
            ori[order[i] - CHAROFFSET] = (char)(mor % val[i]);
        }
        for (; phase < 8; phase += 2)
        {
            for (j = 0; !searchphase(j, 0, 9); j++) ;
            for (i = 0; i < j; i++)
            {
                sOutput += "FBRLUD".charAt(move[i]) + "" + String.valueOf(moveamount[i]);
               // sOutput += " ";
            }
        }
        return fix(sOutput);
    }

    static public String fix(String sInput){
        if(sInput.equals(""))
            return "Solved!";

        String a[]=sInput.split(" "),b[] = new String[a.length];
        int i,j;
        for(i=0, j=0;i<a.length;i++){
            if(i+1<a.length){
                if(a[i].charAt(0)==a[i+1].charAt(0)){
                    int num=(a[i].charAt(1)+a[i+1].charAt(1)-2*'0')%4;
                    if(num!=0){
                        b[j]=String.valueOf(a[i].charAt(0));
                        b[j++]+=String.valueOf(num);
                    }
                    i++;
                }else
                    b[j++]=a[i];
            }else{
                b[j++]=a[i];
            }
        }
        String sOutput="";
        for(i=0;i<j;i++){
            sOutput+=b[i]+" ";
        }
        sOutput.trim();
        return sOutput;
    }
}
