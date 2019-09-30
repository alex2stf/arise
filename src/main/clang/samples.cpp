
#include "samples.h"



int main(){
    A * a = new A();
    a->afiseaza(); //A

    A * b = new B();
    //A, pentru ca nu e virtuala in parinte
    // deci nu se aplica regulile polimorfismului
    b->afiseaza();

    B * b2 = new B();
    b2->afiseaza(); //B

    A2 * a2 = new A2();
    a2->afiseaza(); //A2

    C * c = new C();
    c->afiseaza(); //C pentru ca am implementat

}





