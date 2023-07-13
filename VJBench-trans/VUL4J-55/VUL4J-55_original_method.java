public Calendar ceil(Calendar cal) {
    Calendar twoYearsFuture = (Calendar) cal.clone();
    twoYearsFuture.add(Calendar.YEAR, 2);
    OUTER:
    while (true) {
        if (cal.compareTo(twoYearsFuture) > 0) {
            throw new RareOrImpossibleDateException();
        }
        for (CalendarField f : CalendarField.ADJUST_ORDER) {
            int cur = f.valueOf(cal);
            int next = f.ceil(this,cur);
            if (cur==next)  continue;   
            for (CalendarField l=f.lowerField; l!=null; l=l.lowerField)
                l.clear(cal);
            if (next<0) {
                f.rollUp(cal, 1);
                f.setTo(cal,f.first(this));
                continue OUTER;
            } else {
                f.setTo(cal,next);
                if (f.redoAdjustmentIfModified)
                    continue OUTER; 
            }
        }
        return cal; 
    }
}
