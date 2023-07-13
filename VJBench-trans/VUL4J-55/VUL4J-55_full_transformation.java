public Calendar roundUp(Calendar calendar) {
    Calendar futurityTwoYears = (Calendar) calendar.clone();
    futurityTwoYears.add(Calendar.YEAR, 2);
    EXTERIOR:
    while (true) {
        if (calendar.compareTo(futurityTwoYears) <= 0) {
            int i =0;
            while (i< CalendarAttribute.ADJUST_ORDER.length) {
                CalendarAttribute a = CalendarAttribute.ADJUST_ORDER[i++];
                int now = a.valueOf(calendar);
                int following = a.roundUp(this,now);
                if (now==following)  continue;   

                CalendarAttribute b=a.lowerAttribute;
                while (b!=null){
                    b.empty(calendar);
                    b=b.lowerAttribute;
                }

                if (following>=0) {
                    a.changeTO(calendar,following);
                    if (a.remakeAdaptationWhenAltered)
                        continue EXTERIOR; 
                    
                } else {
                    a.Increment(calendar, 1);
                    a.changeTO(calendar,a.initial(this));
                    continue EXTERIOR;    
                }
            }
            return calendar; 
            
        }else{
            throw new UnusualOrUnrealisticTimeException();
        }

    }
}