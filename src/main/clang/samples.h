#include <iostream>
#include <stdlib.h>

class A {
    public:
        void afiseaza() {
            std::cout << "A" << std::endl;
        };
};

class B : public A {
    public:
        void afiseaza() {
            std::cout << "B" << std::endl;
        };
};


class A2 {
    public:
        void afiseaza() {
            std::cout << "A2" << std::endl;
        };
};

class C: public A, public A2 {
    public:
        //obligat sa implementez, altfel arunca
        //error: request for member ‘afiseaza’ is ambiguous
        void afiseaza(){
            std::cout << "C" << std::endl;
        };
};


class Base {
public:
    Base(){}
    Base( const Base * obj){ };
    virtual Base* clone() const {
        return new Base(this);
    }
};

class Derived : public Base {
public:
    Derived(){}
    Derived( const Derived * obj){ };
    Derived( const Base * obj){ };

    virtual Derived* clone() const {
        return new Derived(this);
    }
};