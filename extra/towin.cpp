#include <iostream>
#include <fstream>
using namespace std;
int main (int argc, char* argv[])
{
	ifstream in ((const char*)argv[1]);
	while (!in.eof ()) {
		char c = in.get ();
		if (c == 0x0a) {
			cout << (char)0x0d;
			cout << (char)0x0a;
		}
		else cout << c;
	}
	return 0;
}
