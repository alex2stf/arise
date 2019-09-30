// compile with gcc -o point point.c

#include "point.h"
#include <stdlib.h>
//#include <math.h>

struct Point {
    //incapsulare perfecta
    //variabilele nu sunt definite in point.h (fisierul de definire)
    double x, y;
};

struct NamedPoint {
    double x, y;
};


struct Point* makePoint(double x, double y) {
    struct Point * res = (struct Point *) malloc(sizeof(struct Point));
    res->x = x;
    res->y = y;
    return res;
};

struct NamedPoint* makeNamedPoint(double x, double y) {
    struct NamedPoint * res = (struct NamedPoint *) malloc(sizeof(struct NamedPoint));
    res->x = x;
    res->y = y;
    return res;
};

double distance(struct Point * p1, struct Point * p2){
    double dx = p1->x - p2->x;
    double dy = p1->y - p2->y;
    return dx + dy;
}

int main(){
    struct Point * p1 = makePoint(20, 20);
    struct Point * p2 = makePoint(30, 30);
    struct NamedPoint * p3 = makeNamedPoint(10, 10);
    struct NamedPoint * p4 = makeNamedPoint(120, 120);
    printf("%f...\n", distance(p1, p2));
    printf(
        "%f...\n",
        distance(
                //cast la point posibil datorita variabilelor cu acelasi nume, exemplu de polimorfism
                (struct Point *)p3,
                (struct Point *)p4
        )
    );
}


/**
 * Created by mrk on 4/7/14.
 */

class Rectangle {
    int getHeight()
    void setHeight(int value)
    int getWidth()
    void setWidth(int value)
}

class Square : Rectangle { }

void invariant(Rectangle r) {
    r.setHeight(200)
    r.setWidth(100)
    assert(r.getHeight() == 200 and r.getWidth() == 100)
}
