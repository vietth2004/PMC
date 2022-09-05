#include <iostream>
#include <cstring>
using namespace std;


/*
https://www.includehelp.com/cpp-programs/find-total-number-of-days-in-given-month-of-year.aspx
C++ program to find/print total number of days in a month, in this program, we will input month, year and program will print total number of days in that month, year.

While writing code in C++ programming language, sometimes we need to work with the date, this program may useful for you.

In this program, we are going to find total number of days in a month, year. Here, we are designing a
function named getNumberOfDays(), function will take month and year as arguments, function will return
total number of days.
*/
int  getNumberOfDays(int month, int year)
{
	//leap year condition, if month is 2
	if( month == 2)
	{
		if (year % 4 == 0) {
                    if (year % 100 == 0) {
                        if (year % 400 == 0)
                            //cout << year << " is a leap year.";
                            return 29;
                        else
                            //cout << year << " is not a leap year.";
                            return 28;
                    }
                    else
                        //cout << year << " is a leap year.";
                        return 29;
                }
                else
                    //cout << year << " is not a leap year.";
                    return 28;
	}
	//months which has 31 days
	else if(month == 1)
	    return 31;
	    else if (month == 3)
	     return 31;
	     else if (month == 5)
	      return 31;
	      else if (month == 7)
	       return 31;
	       else if (month == 8)
	       return 31;
	       else if (month == 10)
	        return 31;
	         else if (month==12)
	         return 31;
                else
                    return 30;
}

/*
Write a C program to find the eligibility of admission for a professional course based on the following criteria:
Marks in Maths >=65
Marks in Phy >=55
Marks in Chem>=50
Total in all three subject >=190
or
Total in Math and Physics >=140

return: 1: eligible, 0: not eligible
*/
int IsEligible(int m, int p, int c)
{
    if (m>=65)
         if(p>=55)
             if(c>=50)
                if((m+p+c)>=190||(m+p)>=140)
                   //printf("The  candidate is eligible for admission.\n");
                   return 1;
                else
                  //printf("The candidate is not eligible.\n");
                  return 0;
             else
                //printf("The candidate is not eligible.\n");
                return 0;
         else
	        //printf("The candidate is not eligible.\n");
	        return 0;
    else
        //printf("The candidate is not eligible.\n");
        return 0;
}

/*
	intput : avg (the average point)
 	output: return charecter correspond to given avg
 	in 90-100, return  A
 	in 80-89, return  B
 	in 70-79, return  C
 	in 60-69, return  D
 	in 0-59, return  F
 	else, return  I

 	Error in function
 	Number of error: 5
	if averageGrade is in {90,80,70,60,0} the function will return unexpected result
*/

char grade(int avg){
	if(avg>=85 && avg <=100)
		return 'A';
	else if(avg>=70 && avg<85)
		return 'B';
	else if(avg>=55 && avg<70)
		return 'C';
	else if(avg>=40 && avg<55)
		return 'D';
	else if(avg>=0 && avg < 40)
		return 'F';
	return 'I';
}


//****************************************************************************
/*
	intput : age, distance
 	output: return integer number correspond to given age, distance

  	 Error in function
 	 Number of error: 4
	if age in {4, 14, 15}, distance=10  the function will return unexpected result
	note: replace
	//if(age >= 4 && age <= 14) by 	if(age > 4 && age < 14)
    //if(age >= 15) by 	if(age > 15)
    //if(distance > 10) by if(distance >= 10)
*/

int getFare(int age, int distance)
{
	int fare;
	//if(age >= 4 && age <= 14)
	if(age > 4 && age < 14)
	{
	    //if(distance > 10)
		if(distance >= 10)
			fare = 130;
		else fare = 100;
	}
	//if(age >= 15)
	if(age > 15)
	{
		if(distance <10 && age >= 60)
			fare = 160;
		else if(distance > 10 && age < 60)
			fare = 250;
			else fare = 200;
	}
	return fare;
}

//****************************************************************************

//Tinh toan cung hoang dao (Zodiac) va du bao
/*
	intput : date, month
 	output: this function return zodiac given date
*/
int calculateZodiac(int date, int month){
   int t;
   {

      if (((month==3)&&(date>=21)&&(date<=31))||((month==4)&&(date<=19)))

      {
         cout << "\n\n\t\t\tYour Zodiac sign is ARIES";
         cout << "\n\n 2012 would be a good year overall. You will experience a rise in financial luck and inflows";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 1;
      }

      else if (((month==4)&&(date>=20)&&(date<=30))||((month==5)&&(date<=20))) {
         cout << "\n\n\t\tYour Zodiac sign is TAURUS";
         cout << "\n\n A very eventful year. Career would be on a high growth trajectory and bring in major progress and achievements.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 2;
      }

      else if (((month==5)&&(date>=21)&&(date<=31))||((month==6)&&(date<=20))) {
         cout << "\n\n\t\tYour Zodiac sign is GEMINI";
         cout << "\n\nA very positive year. Income & professional growth would be immense. You will find the ability to make some very profitable deals now.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 3;
      }

      else if (((month==6)&&(date>=21)&&(date<=30))||((month==7)&&(date<=22))) {
         cout << "\n\n\t\tYour Zodiac sign is CANCER";
         cout << "\n\nA very eventful year, although negative thoughts and unnecessary pessimism could spoil your spirit and bring in unnecessary tension.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 4;
      }

      else if (((month==7)&&(date>=23)&&(date<=31))||((month==8)&&(date<=22))) {
         cout << "\n\n\t\tYour Zodiac sign is LEO";
         cout << "\n\nAn exceptional year again. You will see a rise in status and expansion in career this year too. Luck will favor you throughout.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 5;
      }

      else if (((month==8)&&(date>=23)&&(date<=31))||((month==9)&&(date<=22))) {
         cout << "\n\n\t\tYour Zodiac sign is VIRGO";
         cout << "\n\nA brilliant & positive year, where you will make things happen on your own strength, rather than seeking support of others.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 6;
      }

      else if (((month==9)&&(date>=23)&&(date<=30))||((month==10)&&(date<=22))) {
         cout << "\n\n\t\tYour Zodiac sign is LIBRA";
         cout << "\n\nA powerful phase will be in operation this month. You will find your role as defined by nature will change and all efforts and activities carried out by you will assume higher importance and effectiveness.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 7;
      }

      else if (((month==10)&&(date>=23)&&(date<=31))||((month==11)&&(date<=21))) {
         cout << "\n\n\t\tYour Zodiac sign is SCORPIO";
         cout << "\n\nPositive period would continue, although you need to be careful that throwing good money after bad money is not a great idea.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 8;
      }

      else if (((month==11)&&(date>=22)&&(date<=31))||((month==12)&&(date<=21))) {
         cout << "\n\n\t\tYour Zodiac sign is SAGITTARIUS";
         cout << "\n\n2012 brings in promise and progress. New ideas and cerebral approach to matters will bring in much progress this year. You will be at your creative best till May 2012 and thereafter dynamic activity will take you along.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 9;
      }

      else if (((month==12)&&(date>=22)&&(date<=30))||((month==1)&&(date<=19))) {
         cout << "\n\n\t\tYour Zodiac sign is CAPRICORN";
         cout << "\n\nA very positive year for you. You would be at your creative best and luck related peak in most of the works you get into.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 10;
      }

      else if (((month==1)&&(date>=20)&&(date<=31))||((month==2)&&(date<=18))) {
         cout << "\n\n\t\tYour Zodiac sign is AQUARIUS";
         cout << "\n\nA much better year in comparison to 2010 & 2011. You will feel a surge in your luck, productivity and general sense of positive outlook.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 11;
      }

      else if (((month==2)&&(date>=19)&&(date<=29))||((month==3)&&(date<=20))) {
         cout << "\n\n\t\tYour Zodiac sign is PISCES";
         cout << "\n\nSome amount of struggle and hurdles could come about in life this year. You will have a positive and gainful period till May 2012.";
         cout << "\n\n\t\t Best of luck for Your Future ";
         t = 12;
      }

      else {
         t = -1;
      }

   }

   return t;
}


//****************************************************************************
/* Phan loai dua theo Math va English
Type A: Math + English >=180 and Math >= 50 && English>=60
Type B: Math >= 80 || English >=90 and Math >=50 && English>=60
Type C:  Math>=50 and English>=60  and not in Type A, Type B
Type D: Other
Code:
Add error at boundary,
	Math > 50 && English >60  thay vi Math >= 50 && English >=60
	Math > 80 || English >90 thay vi Math >= 80 || English >=90
Dead code:  if (Math + English >=180) return 'A';
	Khong bao gio tra ve type 'A'

*/
char  MathEnglishGrade (int Math, int English)
{
//Loi ta cac truong hop Math=50, math=80, English=60, English=90
	//if(Math>=50 && English>=60)
	if(Math>50 && English>60)
   	{
		//if(Math>=80 || English>=90)
		if(Math>80 || English>90)
		{
			return 'B';
		}
		else
		{
			if (Math + English>=180)
			{
				return 'A';
			}
			else
			{
		   		return 'C';
			}
		}
    }
    return 'D';
}

//****************************************************************************

/*
 	input: day, month, year
 	output:
 	Tinh so ngay la khoang thoi gian tu ngay 01/01/1900 den ngay day/month/year

 Error:
 	Cay loi tai dieu kien if (year==1990) thanh if (year>1900)
    Khi Year>1900 thi cho ket qua khong mong muon, cau lenh if(year>1900 khong thuc hien

 */
long CDateToNumber(int day, int month, int year)
{
int ngay[13] = {0,31,28,31,30,31,30,31,31,30,31,30,31};
long s, i;
/*

*/
s=0;
if (year > 1900)
{
	s=s+day;
	for (i=0; i < month; i++) s=s+ngay[i];
}
else
	if (year>1900)
	{
		s=s+day;
		s= s+(year-1900)*365;
		for (i=1900; i <= year; i++)
			if (((i%4==0)&&(i%100!=0))||(i%400==0)) s=s+1;
		for (i=0; i < month; i++)
		{
			s= s+ngay[i];
			if (((i%4==0)&&(i%100!=0)||(i%400==0))&&(i==2)) s=s+1;
		}
	}
return s;
}

//****************************************************************************
/*
 	input: hour, minute, second
 	output: Total seconds from 0:0:0 to hour:minute:second
  Error:
 	Cay loi tai dieu kien if (year==1990) thanh if (year>1900)
    Khi Year>1900 thi cho ket qua khong mong muon, cau lenh if(year>1900 khong thuc hien
 */

long CountSecond(int hour, int minute, int second)
{
long s;
//Tinh tong so giay tinh tu luc 0:0:0 den hour:minute:second
//if (hour>=0 && minute>=0&&second>=0&&hour<=24&&minute<=60&&second<=60) da bo het dau bang = trong cac dieu kien don
s=-1;
if (hour>0 && minute>0 && second>0 && hour<24 && minute<60 && second<60)
	s=hour*3600+minute*60+second;
return s;
}

//****************************************************************************

/*
Kiem tra 3 so nhap vao co tao thanh ngay thang hop le khong
 	input: day, month, year
 	output: return 1 if valid date; return 0 if invalid date
   Error:         replace if (day>=1 && month>=1 && year>=1 && day<=31 && month<=12 && year<=2020) --> 6 loi
   by if (day>1 && month>1 && year>1 && day<31 && month<12 && year<2020)
 */
 int CheckValidDate(int day, int month, int year){ //Tuong doi cham
 //if (day>=1 && month>=1 && year>=1 && day<=31 && month<=12 && year<=2020) --> 6 loi
if (day>1 && month>1 && year>1 && day<31 && month<12 && year<2020){
   if((month==1 || month==3 || month==5 || month==7 || month==8 || month==10 || month==12) && (day<=31))  return 1;
   if((month==4 || month==6 || month==9 ||month==11) && (day<=30)) return 1;
   if((month==2) && (day<28))  return 1;   // day<=28
   if((month==2) && (day==29) && (year%4==0)&&(year%400 != 0)) return 1;
}
return 0;
}
//****************************************************************************
/*
Kiem tra 3 so nhap vao co tao thanh thoi gia hop le khong
 	input: hour, minute, second
 	output: return 1 if valid time; return 0 if invalid time
   Error: replace if ((hour>=0 && hour<=23) && (minute>=0 && minute<=59) && (second>=0 && second<=59))  bo tat ca dau bang "=" trong dieu kien don
   by if (hour>0 && hour<23 && minute>0 && minute<59 && second>0 && second<59)
 */

int CheckValidTime(int hour, int minute, int second)
{
if (hour>0 && hour<23 && minute>0 && minute<59 && second>0 && second<59) return 1;
return 0;
}
//****************************************************************************

/*
	input: month, day, year
	output: the next date

*/

long NextDate(int year, int month, int day)
{
int ngay[13] = {0,31,28,31,30,31,30,31,31,30,31,30,31};
day=day+1;
while (day>=365)
{
	 year=year+1;
	if ((year%4==0 && year%100!=0) || (year %400==0))
		day=day-366;
	else
		 day=day-365;
 }

while (day>ngay[month])
 {
	 day=day-ngay[month];
	month=month+1;
 while (month>=12)
 {
	year=year+1;
	month=month%12;
 }
}
 return 0;
}