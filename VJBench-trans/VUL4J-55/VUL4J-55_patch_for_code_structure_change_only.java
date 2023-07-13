    public Calendar ceil(Calendar cal) {
        Calendar twoYearsFuture = (Calendar) cal.clone();
        twoYearsFuture.add(Calendar.YEAR, 2);
        OUTER:
        while (true) {
            if (cal.compareTo(twoYearsFuture) <= 0) {
                int i =0;
                while (i< CalendarField.ADJUST_ORDER.length) {
                    CalendarField f = CalendarField.ADJUST_ORDER[i++];
                    int cur = f.valueOf(cal);
                    int next = f.ceil(this,cur);
                    if (cur==next)  continue;   

                    CalendarField l=f.lowerField;
                    while (l!=null){
                        l.clear(cal);
                        l=l.lowerField;
                    }

                    if (next>=0) {
                        f.setTo(cal,next);
                        if (f.valueOf(cal) != next) {
                            // we need to roll over to the next field.
                            f.rollUp(cal, 1);
                            f.setTo(cal,f.first(this));
                            // since higher order field is affected by this, we need to restart from all over
                            continue OUTER;
                        }

                        if (f.redoAdjustmentIfModified)
                            continue OUTER; 
                       
                    } else {
                        f.rollUp(cal, 1);
                        f.setTo(cal,f.first(this));
                        continue OUTER;    
                    }
                }
                return cal; 
                
            }else{
                throw new RareOrImpossibleDateException();
            }
    
        }
    }
