class GVBA2I {

	public native Integer doA2I(String stringin, Integer beginIndex, Integer len);

		public Integer showA2I(String stringin, Integer beginIndex, Integer len) {
			System.out.println("stringin: " + stringin);
			String strnum = stringin.substring(beginIndex, beginIndex+len);
			System.out.println("strnum: " + strnum);
            System.out.println("beginIndex: " + beginIndex + " len: " +len);
			return Integer.MAX_VALUE;	
		}

	    public Integer doAtoi(String stringin, Integer beginIndex, Integer len) {

			String strnum = stringin.substring(beginIndex, beginIndex+len);

			char str[] = strnum.toCharArray();

			int sign = 1, base = 0, i = 0;
		    while (str[i] == ' ')
		    {
			    i++;
		    }
		    if (str[i] == '-' || str[i] == '+')
		    {
			    sign = 1 - 2 * (str[i++] == '-' ? 1 : 0);
		    }
		    while (i < str.length
			    && i <= len
			    && str[i] >= '0'
			    && str[i] <= '9') {
			    if (base > Integer.MAX_VALUE / 10
				    || (base == Integer.MAX_VALUE / 10
					&& str[i] - '0' > 7))
			    {
				    if (sign == 1)
					    return Integer.MAX_VALUE;
				    else
					    return Integer.MIN_VALUE;
			    }
			    base = 10 * base + (str[i++] - '0');
		    }
		    return base * sign;
	    }
		
		public Integer doAtois(String stringin, Integer beginIndex) {

			int len = stringin.length() - beginIndex;
			if (len < 1) {
				return 0;
			}
			
			String strnum = stringin.substring(beginIndex, beginIndex+len);

			char str[] = strnum.toCharArray();

			int sign = 1, base = 0, i = 0;
		    while (str[i] == ' ')
		    {
			    i++;
		    }
		    if (str[i] == '-' || str[i] == '+')
		    {
			    sign = 1 - 2 * (str[i++] == '-' ? 1 : 0);
		    }
		    while (i < str.length
			    && i <= len
			    && str[i] >= '0'
			    && str[i] <= '9') {
			    if (base > Integer.MAX_VALUE / 10
				    || (base == Integer.MAX_VALUE / 10
					&& str[i] - '0' > 7))
			    {
				    if (sign == 1)
					    return Integer.MAX_VALUE;
				    else
					    return Integer.MIN_VALUE;
			    }
			    base = 10 * base + (str[i++] - '0');
		    }
		    return base * sign;
	    }
}