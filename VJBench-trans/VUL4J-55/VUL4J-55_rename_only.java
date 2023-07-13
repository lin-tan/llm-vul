public Calendar roundUp(Calendar calendar) {
    Calendar futurityTwoYears = (Calendar) calendar.clone();
    futurityTwoYears.add(Calendar.YEAR, 2);
    EXTERIOR:
    while (true) {
        if (calendar.compareTo(futurityTwoYears) > 0) {
            throw new UnusualOrUnrealisticTimeException();
        }
        for (CalendarAttribute a : CalendarAttribute.ADJUST_ORDER) {
            int now = a.valueOf(calendar);
            int following = a.roundUp(this,now);
            if (now==following)  continue;   
            for (CalendarAttribute b=a.lowerAttribute; b!=null; b=b.lowerAttribute)
                b.empty(calendar);
            if (following<0) {
                a.Increment(calendar, 1);
                a.changeTO(calendar,a.initial(this));
                continue EXTERIOR;
            } else {
                a.changeTO(calendar,following);
                if (a.remakeAdaptationWhenAltered)
                    continue EXTERIOR; 
            }
        }
        return calendar; 
    }
}