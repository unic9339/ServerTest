/*
*  revised in 20200928
*  (1)mystrindex(): used for json send by SpringBoot, which numeric keyvalue is noe included by double quotes
*  struct xGW_DEF: plant_id[4] will hold coorect "p001", plant_id[5] will. Since 0 terminated will use one index
*
*
*
*/

//#include "stdafx.h"
// #include <windows.h>
// #include <winsock2.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <unistd.h>
#include <stdbool.h>
#include <time.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <memory.h>
#include <json.h>

#include "cJSON.h"

 
// #pragma  comment(lib,"ws2_32.lib")
// #define BUFF_SIZE 1024

#define PORT 1225
#define IP_ADDR "127.0.0.1"

#define NUM_THREAD 2
#define NUMBER_MBDATA 32
#define NUMBER_MBID 2

typedef struct xGW_DEF{
    char esn[50];    //serial no
	char plant_id[5];
	char name[50];	
	char smlogger_esn[50]; //smartlogger serial no
    uint32_t baud_rate;
    uint8_t data_bit;
    char parity[8];
    uint8_t stop_bit;
    char flowc[8];
	uint16_t sam_t;
	char user_id[6];
	uint8_t version;	
}xGwDef;

typedef enum KeyType{
  Cha = 0,
  UI8,
  UI16,
  UI32
}xKeyType;

typedef struct xGWStruct{
    char name[50];    //serial no
	char type;
}xGwStruct;


typedef struct xMSGHEADER_DEF{
    uint8_t magic_no[2];
	uint8_t msg_suffix[2];
	uint8_t version;
	uint8_t msg_type;
	short length;
}xMsgHeaderDef;

typedef enum MsgTypeEnum{
	REQ_GW = 1,
	RES_GW,
	REQ_DEVLIST,
	RES_DEVLIST,
	REQ_DEVCT,
	RES_DEVCT,
	REQ_DEVS,
	RES_DEVS,
	REQ_SML,
	RES_SML,
	REQ_MBLIST,
	RES_MBLIST,
	REQ_MBCT,
	RES_MBCT,
	REQ_MBS,
	RES_MBS,
	REQ_MB,
	RES_MB,
	PING,
    PONG
}xMsgTypeEnum;


typedef struct xDEVICE_DATA{
  uint8_t name[20];    //name
  uint8_t data[30];   //char's value 
}xDeviceData;

typedef struct xMYSONARRAY_DEF{
  char count;
  int arr_addr[34][2]; // saving the start index of each array, max 34, [0][0]: start index, [0][1]:string length
}xMysonarrayDef;


void hextostr(uint8_t *headers, uint8_t *header, size_t header_len);
void tohex(uint8_t * in, size_t insz, uint8_t * out, size_t outsz);
// int request0(SOCKET socket, char *sendData, char *recvBuf, int size);
// int request(socket, char *recvBuf, int size);
int mystrindex(char recv[], char *key1, uint16_t *key2, uint32_t *key3, char *keyname, int *arraddr, char type);
int mystrindex_test(char recv[], char *key1, uint16_t *key2, uint32_t *key3, char *keyname, int *arraddr, char type);
int mysonastrs(char s[], xMysonarrayDef *mysonarray);
void print(xMysonarrayDef *mysonarray);
int type_digit(xKeyType kt);
void JsonToGw(char recvBuf[], xGwDef *gw, xGwStruct (*gwstruct)[2], int *arraddr);

static cJSON *create_json_object(xDeviceData *dev_data, int num)
{
	int i;
	cJSON *root = NULL;
	root = cJSON_CreateObject();
 
	for(i = 0; i < num; i++){
		cJSON_AddStringToObject(root, dev_data[i].name, dev_data[i].data);
	}
 
	return root;
}

int main(int argc, char* argv[])
{
    int iRet;
	
	uint8_t GW_ESN[30] = "open107vstm32f107vct6";
	uint8_t header[10];
    uint8_t body[30] = "open107vstm32f107vct6";
	uint8_t sendData[60] = {0};
	uint8_t sendData2[60] = {0};
	
	xGwStruct gwstruct[][2] = {
		{"esn", Cha},
		{"plant_id",Cha},
		{"name",Cha},
		{"smlogger_esn",Cha},
		{"baud_rate",UI32},
		{"data_bit",UI8},
		{"parity",Cha},
		{"stop_bit",UI8},
		{"flowc",Cha},
		{"sam_t",UI8},
		{"user_id",Cha},
		{"version",UI8}
	};
	
    cJSON *root = NULL;
	char *payload = NULL;
	
	xDeviceData deviceData0;	// collected data
		//collected data
	//deviceData0 = (xDeviceData *)malloc(sizeof(xDeviceData) * 1);
	
    strcpy(deviceData0.name, "esn"); 
	strcpy(deviceData0.data, body); 
	
	
	//self defined Tcp instruction, including symbal of start and end 
	uint8_t gwst_gw[] = "gwst_gw";          // quering a gw dada by esn
	uint8_t gwed[] = "_gwed";  //end symbal
  
    size_t pos = 0;
    int i, j, k, c, len, mysoncount;
	mysoncount = 0;
	
	
	//int buf_size = 2048; //not enough for decode modbus json from web server
	int buf_size = 3072;
    uint8_t *recvBuf;
    //char recvBuf[buf_size];
	uint8_t recvData[buf_size];
	uint8_t *recvb;
	  
	int recv_size, total_size = 0;
	
	const short datalen_mark = 9472;
	short data_len;
	
	
    root = create_json_object(&deviceData0, 1);
	//payload = cJSON_Print(root);  //having formating(space, return code...)
	payload = cJSON_PrintUnformatted(root);  //without formating
	
	printf("payload: %s\n", payload);
	printf("payload len: %d\n", strlen(payload));
	  
	recvBuf = (uint8_t*)malloc(buf_size*sizeof(uint8_t));
	if(recvBuf == NULL)                     
    {
        printf("Error! memory not allocated.");
        exit(0);
    }
	
    // WORD sockVersion = MAKEWORD(2,2);
    // WSADATA data; 
    // if(WSAStartup(sockVersion, &data) != 0)
    // {
    //     return 0;
    // }

	struct sockaddr_in serAddr;
	char buff[buf_size+5];

// use for mac
	int client_socket;
	client_socket  = socket( PF_INET, SOCK_STREAM, 0);
	if( -1 == client_socket)
	{
		printf( "socket failt\n");
		exit( 1);
	}

    serAddr.sin_family = AF_INET;
    serAddr.sin_port = htons(PORT);
    // serAddr.sin_addr.S_un.S_addr = inet_addr(IP_ADDR); 
	serAddr.sin_addr.s_addr= inet_addr(IP_ADDR);

    uint8_t messagetype[2] = {REQ_GW, REQ_DEVLIST};
	
	xMysonarrayDef mysonarray;
    //memset(&mysonarray, 0, sizeof(xMysonarrayDef));
	//print(&mysonarray);
	
	xGwDef gw0;
	//malloc(sizeof(gw0));
    
for (k = 0; k < 1; k++) {
	 
    // SOCKET sclient = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    // sclient -> serAddr
	client_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

    // if(client_socket == INVALID_SOCKET)
    // {
    //     printf("invalid socket!");
    //     return 0;
    // }
	
    if (connect(client_socket, (struct sockaddr *)&serAddr, sizeof(serAddr)) == -1)
    {
        printf("connect error !");
        closesocket(client_socket);
        return 0;
    }
	printf("\r\nConnnected!\r\n");
	
	xMsgHeaderDef msgheader = {
		.magic_no[0] = 0xFF,
		.magic_no[1] = 0xFE,
		.version = 0x01,
		.msg_type = messagetype[k],
		.length = 0x0015,
		.msg_suffix[0] = 0xFF,
		.msg_suffix[1] = 0xFE,
	};
	
	/*
	for (i = 0; i < strlen(GW_ESN); i++) {
	    body[i] = GW_ESN[i];
	}
    body[i] = '\0'; //terminate
	*/

    //msgheader.length = strlen(body);
	//printf("magic_no: %x\n", msgheader.magic_no);
	//printf("msg_suffix: %x\n", msgheader.msg_suffix);
	//printf("body's length hex: %x\n", msgheader.length);
  	//printf("body's length decimal: %d\n", msgheader.length);
	//printf("body's length decimal: %d\n", msgheader.length>>8 & 0xFF);
	//printf("body's length decimal: %d\n", msgheader.length & 0xFF);
	//printf("sendData[4] 0: %x\n", sendData[4]);
	//printf("sendData[5] 0: %x\n", sendData[5]);
	
	header[0] = msgheader.magic_no[0];
	header[1] = msgheader.magic_no[1];
	header[2] = msgheader.version;
	header[3] = msgheader.msg_type;
	char a = msgheader.length>>8 & 0xFF;
	char b = msgheader.length & 0xFF;
	header[4] = a;
	header[5] = b;
	header[6] = msgheader.msg_suffix[0];
	header[7] = msgheader.msg_suffix[1];	
    //header[6] = '\0'; //terminate
	//printf("header length: %d\n", strlen(header));
	//for (i = 0; i<strlen(header); i++) {
	//	printf("header[%d]: %x\n", i, header[i]);
	//}
	//printf("header[4]: %x\n", header[4]);
	//printf("header[5]: %x\n", header[5]);
	printf("header's hex format: %x\n", header);
	printf("body's string format: %s\n", body);
	
	memset(sendData, 0, sizeof(sendData)); //clear the variable
	
	//cascate header, body, suffix for sending

    uint8_t header1[6];
	uint8_t header2[2];
	
	uint8_t header1_hex[20];
	uint8_t header2_hex[20];
	
	short payload_len = strlen(payload);
	printf("payload length: %d, (hex)%x\n", payload_len , payload_len );
	uint8_t a1 = payload_len >>8 & 0xFF;;
	uint8_t b1 = payload_len  & 0xFF;
	printf("a1: %x, b1: %x\n", a1, b1);
	
	header1[0] = msgheader.magic_no[0];
	header1[1] = msgheader.magic_no[1];
	header1[2] = msgheader.version;
	header1[3] = msgheader.msg_type;
	header1[4] = a1;
	header1[5] = b1;

	header2[0] = msgheader.msg_suffix[0];
	header2[1] = msgheader.msg_suffix[1];
	
	//check how header's hex array will be dislayed in string array
	uint8_t *header1s = malloc(sizeof(header1) * 2 + 1);
	hextostr(header1s, header1, sizeof(header1));
    printf("header1s: %s\n",header1s);
	
	uint8_t *header2s = malloc(sizeof(header2) * 2 + 1);
 	hextostr(header2s, header2, sizeof(header2));
    printf("header2s: %s\n",header2s);
	
	printf("header->body->suffix cascate start:\n");
	memset(sendData2, 0, sizeof(sendData)); //clear the variable	
	strcat(sendData2, header1s);
	printf("header(sendData20): %s\n", sendData2);
	strcat(sendData2, payload);
	printf("header+body(sendData21): %s\n", sendData2);
	strcat(sendData2, header2s);
	printf("header+body+suffix(sendData22): %s\n", sendData2);
	
	printf(" \r\n");
	//check
	uint8_t temp4[sizeof(sendData2)* 3];
    tohex(sendData2, sizeof(sendData2), temp4, sizeof(sendData2) * 3);   //convert to byte array to hexadecimal array, 2020/02/27
	printf("send packe's hex fornat: %s\n", temp4);


/*
	//cascate header, body, suffix for sending
	// seems not to work
    strcat(sendData, header1);
	printf("header: %s\n", sendData);
	strcat(sendData, payload);
	printf("header+body: %s\n", sendData);
	strcat(sendData, header2);
	printf("header+body+suffix: %s\n", sendData);

*/

	for (i = 0; i < 6; i++) {
		sendData[i] = header1[i];
		//printf("sendDta[%d]: %x\n", i, sendData[i]);
	}
	
	for (i = 6; i < 6+payload_len; i++) {
		sendData[i] = payload[i-6];
		//printf("sendDta[%d]: %x\n", i, sendData[i]);
	}
	
	for (i = 6+payload_len; i < 6+payload_len+2; i++) {
		sendData[i] = header2[i-payload_len-6];
		//printf("sendDta[%d]: %x\n", i, sendData[i]);
	}
	sendData[i] = '\0';  //terminate

    //check
	printf("\r\n");	
	uint8_t temp5[sizeof(sendData)* 3];
    tohex(sendData, sizeof(sendData), temp5, sizeof(sendData) * 3);   //convert to byte array to hexadecimal array, 2020/02/27
	printf("sendData's hex fornat: %s\n", temp5);
	
	send(client_socket,sendData,sizeof(sendData),0);
	
    //stop send output data to sever, so server will get the data by completing receive()
    shutdown(client_socket, 1);
	// recv_size = request(serAddr, recvBuf, buf_size);

	//***********/
	int len = 0;
  	char temp_buf[buf_size];

  	int ij = 0;
	while(1)
	{
		memset(temp_buf, 0, buf_size); //clear the variable
		//printf("\r\nij: %d, temp_buf: %s\r\n", ij, temp_buf);
		if(len = recv(socket, temp_buf, buf_size, 0) <= 0 )
		{
		break;
		}
		else
		{
		//strncpy(recvData, recvBuf, ret);
		//recvData[ret+1] = '\0';
		strncpy(recvBuf, temp_buf, buf_size);
		printf("\r\nrecieved packet by recvBuf: %s\r\n", recvBuf);
		}
		ij++;
	}
	//strncpy(recvBuf, recvBuf2, strlen(recvBuf2));
	closesocket(socket);


	
	
	memset(&mysonarray, 0, sizeof(xMysonarrayDef));
	mysoncount = mysonastrs(recvBuf, &mysonarray);
	//printf("mysoncount: %d\n", mysoncount);
	
    //see if completed data is received by checking the starting and ending remark;
	if ((recvBuf[0] == msgheader.magic_no[0]) && (recvBuf[1] == msgheader.magic_no[1])) {
	    printf("\r\nreceived magic_no. is ok!!\r\n");
	} else {
	    printf("\r\nreceived magic_no. is not correct!!\r\n");
	}
	printf("\r\nreceived header's version: %d\n", recvBuf[2]);	
	printf("\r\nreceived header's type: %d\n", recvBuf[3]);
	if ((recvBuf[4]<<8 & 0xff00) != datalen_mark) {
		 data_len = (short)((recvBuf[4]<<8 & 0xff00) + recvBuf[5]);
		 	 printf("\r\nreceived header's data_len: %d\n", ((recvBuf[4]<<8 & 0xff00) + recvBuf[5]));	
    } else{
	     data_len = (short)recvBuf[5];
		 printf("\r\nreceived header[5] data_len: %d\n", recvBuf[5]);
    }
	printf("\r\ndata_len: %d\n", data_len);
	 
	//setting gw's members by exacting keyvalue from json 
    JsonToGw(recvBuf, &gw0, gwstruct, mysonarray.arr_addr[0]);

		
    //gw
    printf("\n\n");
    printf("gw:\n");
    printf("esn: %s\n",gw0.esn);
	printf("plant_id: %s\n",gw0.plant_id);
	printf("name: %s\n",gw0.name);
    printf("smlogger_esn: %s\n",gw0.smlogger_esn);
    printf("baud_rate: %d\n",gw0.baud_rate); 
    printf("data_bit: %d\n",gw0.data_bit); 
    printf("parity: %s\n",gw0.parity);
    printf("stop_bit: %d\n",gw0.stop_bit); 
    printf("flowc: %s\n",gw0.flowc);
    printf("sam_t: %d\n",gw0.sam_t);
    printf("user_id: %s\n",gw0.user_id);
    printf("version: %d\n",gw0.version);  
	
	
	free(header1s);
	free(header2s);
}

  cJSON_Delete(root);
  free(payload); 
  
//   WSACleanup();
  return 0;
}

void hextostr(uint8_t *headers, uint8_t *header, size_t header_len)
{
    for (size_t i = 0; i < header_len; i++)
        sprintf(headers + i * 2, "%02x", header[i]);
}


void tohex(uint8_t * in, size_t insz, uint8_t * out, size_t outsz)
{
    unsigned char * pin = in;
    const char * hex = "0123456789ABCDEF";
    uint8_t * pout = out;
    for(; pin < in+insz; pout +=3, pin++){
        pout[0] = hex[(*pin>>4) & 0xF];
        pout[1] = hex[ *pin     & 0xF];
        pout[2] = ' ';
        if (pout + 3 - out > outsz){
            /* Better to truncate output string than overflow buffer */
            /* it would be still better to either return a status */
            /* or ensure the target buffer is large enough and it never happen */
            break;
        }
    }
		//pout[0] = hex[(*pin>>4) & 0xF];
    //pout[1] = hex[ *pin     & 0xF];
    //pout[2] = ' ';
		
    pout[-1] = 0;
}

//received data from server
// int request(socket,char *recvBuf, int size) 
// {
//   int len = 0;
//   char temp_buf[size];

//   int ij = 0;
//   while(1)
//   {
//     memset(temp_buf, 0, size); //clear the variable
//     //printf("\r\nij: %d, temp_buf: %s\r\n", ij, temp_buf);
//     if(len = recv(socket, temp_buf, size, 0) <= 0 )
// 	{
// 	  break;
// 	}
// 	else
// 	{
// 	  //strncpy(recvData, recvBuf, ret);
// 	  //recvData[ret+1] = '\0';
// 	  strncpy(recvBuf, temp_buf, size);
//       printf("\r\nrecieved packet by recvBuf: %s\r\n", recvBuf);
// 	}
// 	ij++;
//   }
//   //strncpy(recvBuf, recvBuf2, strlen(recvBuf2));
//   closesocket(socket);
//   return len;
// }

//received data from server
// int request0(socket, char *sendData, char *recvBuf, int size) 
// {
//   int len = 0;
//   char temp_buf[size];
//   //printf("\r\nsendData: %s\r\n", sendData);  
//   //send(socket,sendData,sizeof(sendData),0);
//   //stop send output data to sever, so server will get the data by completing receive()
//   //shutdown(socket, 1);
//   int ij = 0;
//   while(1)
//   {
//     memset(temp_buf, 0, size); //clear the variable
//     //printf("\r\nij: %d, temp_buf: %s\r\n", ij, temp_buf);
//     if(len = recv(socket, temp_buf, size, 0) <= 0 )
// 	{
// 	  break;
// 	}
// 	else
// 	{
// 	  //strncpy(recvData, recvBuf, ret);
// 	  //recvData[ret+1] = '\0';
// 	  strncpy(recvBuf, temp_buf, size);
//       printf("\r\nrecieved packet by recvBuf: %s\r\n", recvBuf);
// 	}
// 	ij++;
//   }
//   //strncpy(recvBuf, recvBuf2, strlen(recvBuf2));
//   closesocket(socket);
//   return len;
// }



/*
 * specify the gw struct data from Json received from server, 2020/09/28
 * 
*/
void JsonToGw(char recvBuf[], xGwDef *gw0, xGwStruct (*gwstruct)[2], int *arraddr)
{
	int i;
	int type;
	
	if(mystrindex(recvBuf, gw0->esn, 0, 0, "esn", arraddr, Cha) >= 0) {
	    //printf("gw.esn: %s\n", gw0.esn);
	}

	if(mystrindex(recvBuf, gw0->plant_id, 0, 0, "plant_id", arraddr, Cha) >= 0) {
	    //printf("gw.plant_id: %s\n", gw0.plant_id);
		//printf("plant_id: %s\n",gw0.plant_id);
	}
	if(mystrindex(recvBuf, gw0->name, 0, 0, "name", arraddr, Cha) >= 0) {
	    //printf("gw.name: %s\n", gw0.name);
	}
	if(mystrindex(recvBuf, gw0->smlogger_esn, 0, 0, "smlogger_esn", arraddr, Cha) >= 0) {
	    //printf("gw.smlogger_esn: %s\n", gw0.smlogger_esn);
	}
	if(mystrindex(recvBuf, 0, 0, &gw0->baud_rate, "baud_rate", arraddr, UI32) >= 0) {
	    //printf("gw.baud_rate: %d\n", gw0.baud_rate);
	}
	if(mystrindex(recvBuf, &gw0->data_bit, 0, 0, "data_bit", arraddr, UI8) >= 0) {
	    //printf("gw.data_bit: %d\n", gw0.data_bit);
	}
	if(mystrindex(recvBuf, gw0->parity, 0, 0, "parity", arraddr, Cha) >= 0) {
	    //printf("gw.parity: %s\n", gw0.parity);
	}
	if(mystrindex(recvBuf, &gw0->stop_bit, 0, 0, "stop_bit", arraddr, UI8) >= 0) {
	    //printf("gw.stop_bit: %d\n", gw0.stop_bit);
	}
	if(mystrindex(recvBuf, gw0->flowc, 0, 0, "flowc", arraddr, Cha) >= 0) {
	    //printf("gw.flowc: %s\n", gw0.flowc);
	}
	if(mystrindex(recvBuf, 0, &gw0->sam_t, 0, "sam_t", arraddr, UI16) >= 0) {
	    //printf("gw.sam_t: %d\n", gw0.sam_t);
	}
	if(mystrindex(recvBuf, gw0->user_id, 0, 0, "user_id", arraddr, Cha) >= 0) {
	    //printf("gw.user_id: %s\n", gw0.user_id);
	}
	if(mystrindex(recvBuf, &gw0->version, 0, 0, "version", arraddr, UI8) >= 0) {
	    //printf("gw.version: %d\n", gw0.version);
	}
	

/*	
	for (i = 0; i < 12; i++) {
		type = gwstruct[i]->ktype;
		switch(type){
		   case 0:
		      mystrindex(recvBuf, gw0->name, 0, 0,  gwstruct[i]->name, arraddr, gwstruct[i]->ktype);
		      break;
		   case 1:
			  break;
		   case 2:
			  break;
		   case 3:
			  break;
		   default:
		      printf("aaa\n");
		}
	}
	
	*/
}


/*
 * extract data from Json, 2020/03/27
 * revise for new json from SpringBoot, where value with numeric type is not included by double quotes.
*/
int mystrindex_test(char recv[], char *key1, uint16_t *key2, uint32_t *key3, char *keyname, int *arraddr, char type)
{
	int i, j, k, st, a, b, c, d, e;
	i = j = k = st = a = b = c = d = e = 0;
	int start, length, temp_len;
	start = arraddr[0];
	length = arraddr[1];
	temp_len = 0;
    //printf("recv: %s\n", recv);
    //printf("key: %s\n", key);
    //printf("keyname: %s\n", keyname);
	//printf("start: %d\n", start);
	//printf("length: %d\n", length);
	for (i = start; i < start + length; i++) {  //i < start + length; whole span of json
	    //go beyond the keyname matched, 
		//and stop at the ':' which is after '\"'
		//and keyname[k] != null 
		for (j = i, k = 0; keyname[k] != '\0' && recv[j] == keyname[k]  && recv[i-1] == '\"'; j++, k++) {
			;
		}
		if (k > 0 && keyname[k] == '\0') {  // condition that keyname has been match completely.
	       for (st = i; st < start + length; st++){  // start from first character of matched keyname.
	          //printf("s[%d]: %x\n", st, s[st]);
	       	  if (recv[st] == ':') { //':', 0x3a
	       	  	 //printf("recv[%d]: %x\n", st, recv[st]);
	       	     a = st; //remember the position of ':'
	       	  } 
	       	  if ((recv[st] == ',') && (b == 0)) { //first "," that show finish position of keyvalue 
	       	     c = st; //remember the position 0f ','
	       	  	 b = b + 1;
	       	     printf("b: %d\n", b);
	       	  	 if (type == Cha) {
	       	  	    // b == 3 => reached end quotes(third ") of value of keyname of json string
	       	  	    c = st;
	       	  	 	//memset(key[0], 0, sizeof(key[0])); //clear the key
	       	  	 	//printf("key[0]: %s\n", key[0]);
	       	  	    for (d = a+2; d < c-1; d++) { //copy
	       	  	 	   key1[e] = recv[d];
	       	  	       //printf("recv[%d]: %x\n",d, recv[d]);
	       	  	       //printf("key[%d]: %x\n", e, key[e]);
					   e++;
	       	  	    }
 	                key1[e] = '\0'; //terminate
                    //printf("key: %s\n", key);
	       	  	 } else {
	       	  	    temp_len = type_digit(type);
	       	  	    char temp1[temp_len];
	       	  	   	memset(temp1, 0, sizeof(temp1)); //clear the variable
	       	  	    for (d = a+2; d < c; d++) {
	       	  	 	   	temp1[e] = recv[d];
	       	  	       	e++;
	       	  	    }
 	                temp1[e] = '\0'; //terminate
	       	  	 	if (type == UI8) {
                    	*key1 = (char)atoi(temp1);
                        //printf("*key1: %d\n", *key1);
	       	  	 	} else if (type == UI16)  {
                    	*key2 = (uint16_t)atoi(temp1);
                        //printf("*key2: %d\n", *key2);
	       	  	 	} else if (type == UI32)  {
                    	*key3 = (uint32_t)atoi(temp1);
                        printf("*key3: %d\n", *key3);
	       	  	 	} else {
	       	  	    }					   
				 }
			  }
	       }
	    }
		return 1;
	}
	return -1;
}

/*
 * extract data from Json, 2020/03/27
 * 20200928,revised for new json from SpringBoot, where keyvalue with numeric type is not included by double quotes.
*/
int mystrindex(char recv[], char *key1, uint16_t *key2, uint32_t *key3, char *keyname, int *arraddr, char type)
{
	int i, j, k, st, a, b, c, d, e, f;
	i = j = k = st = a = b = c = d = e = f = 0;
	int start, length, temp_len;
	start = arraddr[0];
	length = arraddr[1];
	temp_len = 0;
    //printf("recv: %s\n", recv);
    //printf("key: %s\n", key);
    //printf("keyname: %s\n", keyname);
	//printf("start: %d\n", start);
	//printf("length: %d\n", length);
	for (i = start; i < start + length; i++) {  //i < start + length; whole span of json
		//go till the keyname matched, 
		for (j = i, k = 0; keyname[k] != '\0' && recv[j] == keyname[k]  && recv[i-1] == '\"'; j++, k++) {
			;
		}
		//find the keyname
		if (k > 0 && keyname[k] == '\0') {  // condition that keyname has been match completely.
	       for (st = i; st < start + length; st++){  // start from first character matched keyname 
	          //printf("s[%d]: %x\n", st, s[st]);
	       	  if (recv[st] == ':') { //':', 0x3a
	       	  	 //printf("recv[%d]: %x\n", st, recv[st]);
	       	     a = st; //remember the position of ':'
		      } else if (recv[st] == '}')  {
				 f = st;
				 //printf("find end mark: recv[%d]: %x\n", f, recv[st]);
	       	  } else {
			  }
			  //find the keyvalue related keyname
	       	  if ((recv[st] == '\"') || (recv[st] == '}') ) { //'\"',0x22, first quotes after ":"
	       	  	 b = b + 1;
				 //printf("find end mark: b:: %d, f: %d, recv[%d]: %x\n", b, f, st, recv[st]);
	       	     //printf("b: %d\n", b);
				 /************************************
				 * keyvalue is char[] type, 20200928
				 *   "keyname1":"keyvalue1","keyname2":"keyvalue2",
				 *            ↑↑          ↑
				 * 　　　　　　　b=1,a          b=3
				 *************************************
				 */
	       	  	 if (b == 3) {  // reached end quotes(third one) of value of keyname of json string
	       	  	    c = st;
					//printf("b=3\n");
					//printf("a: %d, b: %d, c: %d, f: %d\n", a, b, c, f);
	       	  	 	if (type == Cha) {
	       	  	 		//memset(key[0], 0, sizeof(key[0])); //clear the key
	       	  	 	    //printf("key[0]: %s\n", key[0]);
	       	  	        for (d = a+2; d < c; d++) { //copy
	       	  	 	        key1[e] = recv[d];
	       	  	           //printf("recv[%d]: %x\n",d, recv[d]);
	       	  	           //printf("key[%d]: %x\n", e, key[e]);
	       	  	           e++;
	       	  	        }
 	                    key1[e] = '\0'; //terminate
                        //printf("key: %s\n", key);
	       	  	 	} else {
	       	  	    }
	       	  	 }
				 /************************************
				 * keyvalue is numeric type, 20200928
				 *   "keyname1":keyvalue1,"keyname2":keyvalue2,
				 *            ↑↑          ↑
				 * 　　　　　　　b=1,a          b=2
				 *************************************
				 */			 
                 if ((b == 2) && (f == 0)) {
	       	  	    c = st; 
					//printf("b=2, f=0\n");
					//printf("b: %d, c: %d, f: %d\n", b, c, f);
	       	  	 	if (type != Cha)  {
	       	  	 		temp_len = type_digit(type);
	       	  	    	char temp1[temp_len];
	       	  	   		memset(temp1, 0, sizeof(temp1)); //clear the variable
	       	  	    	for (d = a+1; d < c-1; d++) {
	       	  	 	   		temp1[e] = recv[d];
	       	  	       		e++;
	       	  	    	}
 	                	temp1[e] = '\0'; //terminate
	       	  	 		if (type == UI8) {
                    	    *key1 = (char)atoi(temp1);
                            //printf("*key1: %d\n", *key1);
	       	  	 		} else if (type == UI16)  {
                    	    *key2 = (uint16_t)atoi(temp1);
                            //printf("*key2: %d\n", *key2);
	       	  	 		} else if (type == UI32)  {
                    	    *key3 = (uint32_t)atoi(temp1);
                            //printf("*key3: %d\n", *key3);
	       	  	 		} else {
	       	  	 		}
	       	  	   }					
                 }
				 /************************************
				 * keyvalue is numeric type, 20200928
			     *   "keyname1":keyvalue1}
				 *            ↑↑         ↑
				 * 　　　　　　　b=1,a         b=2
				 *************************************
				 */			 
                 if ((b == 2) && (f != 0)) {
	       	  	    c = st;
					//printf("b=2, f!=0\n");
					//printf("b: %d, c: %d, f: %d\n", b, c, f);
	       	  	 	if (type != Cha)  {
	       	  	 		temp_len = type_digit(type);
	       	  	    	char temp1[temp_len];
	       	  	   		memset(temp1, 0, sizeof(temp1)); //clear the variable
	       	  	    	for (d = a+1; d < f; d++) {
	       	  	 	   		temp1[e] = recv[d];
	       	  	       		e++;
	       	  	    	}
 	                	temp1[e] = '\0'; //terminate
	       	  	 		if (type == UI8) {
                    	    *key1 = (char)atoi(temp1);
                            //printf("*key1: %d\n", *key1);
	       	  	 		} else if (type == UI16)  {
                    	    *key2 = (uint16_t)atoi(temp1);
                            //printf("*key2: %d\n", *key2);
	       	  	 		} else if (type == UI32)  {
                    	    *key3 = (uint32_t)atoi(temp1);
                            //printf("*key3: %d\n", *key3);
	       	  	 		} else {
	       	  	 		}
	       	  	   }					
                 }				 
	       	  }
	       }
		   return 1;
		}
	}
	return -1;
}

/* 
 * added the description, in 20200927
 * find the index of '{' of json start character, and the length of included by '{}'
 * arg s[]: received data from server. The body of data is json formart.
 * arg mysonarray: hold start index of '{', and the length of each json data
 * return the count of pair of '{}'
*/ 
int mysonastrs(char s[], xMysonarrayDef *mysonarray) 
{
    int i, j, k, count, length;
	int start[NUMBER_MBDATA] = {0};
	i = j = k = count = length = 0;
	for (i = 0; s[i] != '\0'; i++) {
	    if (s[i] == '{') { // 0x7b
	        start[j] = i;  //start[0] save first index of '{'
	        j++;
		} else if(s[i] == '}') {  // 0x7d
			k++;
		    if ((k == j)) {
		       length = i - start[j-1] + 1;  //length from '{' to ']'
	           mysonarray->count++;
	           mysonarray->arr_addr[j-1][0] = start[j-1];
	           mysonarray->arr_addr[j-1][1] = length;
		       //printf("start[%d]: %d\n", j-1, start[j-1]);
		       //printf("mysonarray->arr_addr[%d][%d]: %d\n", j-1, 0, mysonarray->arr_addr[j-1][0]);
		       //printf("mysonarray->arr_addr[%d][%d]: %d\n", j-1, 1, mysonarray->arr_addr[j-1][1]);
		       //printf("length: %d\n", length);		    
		    }
		} else {
		}
	}
	//printf("count: %d\n", mysonarray->count);
	//count of pattern '{xxx...}'
	return mysonarray->count;
}


void print(xMysonarrayDef *mysonarray)
{
   int i, j;
   printf("count: %d\n",mysonarray->count);
   for (i = 0; i < NUMBER_MBDATA; i++)
   {
   	   for (j = 0; j < 2; j++) {
   	       printf("arr_addr[%d][%d]: %d\n", i, mysonarray->arr_addr[i][j]);
   	   }
   }
}

int type_digit(xKeyType kt) {
    int digit;	
	switch (kt) {
	case Cha:
		digit = 0;
		break;
	case UI8:
		digit = 3;
		break;
	case UI16:
		digit = 5;  //for max tcp port number
		break;
	case UI32:
		digit = 6; // for max baud_reate:115200
		break;
	}
	return digit;
}