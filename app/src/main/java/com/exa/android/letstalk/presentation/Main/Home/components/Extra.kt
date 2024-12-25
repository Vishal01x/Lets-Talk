package com.exa.android.letstalk.presentation.Main.Home.components


/*
@Composable
fun HomeListItem(chat: String, zoomImage: (Int) -> Unit, openChat: () -> Unit) {
     Card(
          elevation = CardDefaults.cardElevation(8.dp),
          modifier = Modifier
              .padding(vertical = 8.dp, horizontal = 16.dp)
              .fillMaxWidth()
              .clickable {
                  openChat()
              },
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)

      ) {
          Row(
              modifier = Modifier
                  .padding(16.dp)
                  .fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
          ) {
              Image(
                  painter = painterResource(id = R.drawable.ic_launcher_background),
                  contentDescription = "Profile Picture",
                  modifier = Modifier
                      .size(56.dp)
                      .clip(CircleShape)
                      .shadow(8.dp, shape = CircleShape)
                      .clickable {
                          zoomImage(R.drawable.ic_launcher_background)
                      },
                  contentScale = ContentScale.Crop
              )

              Spacer(modifier = Modifier.width(12.dp))

              Column(
                  modifier = Modifier
                      .weight(1f)
                      .padding(end = 8.dp)

              ) {
                  Text(
                      text = chat,
                      fontSize = 16.sp,
                      maxLines = 1,
                      fontWeight = FontWeight.Bold,
                      overflow = TextOverflow.Ellipsis
                  )
                  Text(
                      text = "What is the status of your work What is the status of your work",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      style = MaterialTheme.typography.bodyMedium,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                  )
              }
              Column(
                  horizontalAlignment = Alignment.End,
                  verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                  Text(
                      text = "12.20",
                      fontSize = 12.sp,
                      color = Color.Gray
                  )
                  val unreadMessage = 2
                  if (unreadMessage > 0) {
                      Box(
                          modifier = Modifier
                              .size(24.dp)
                              .background(Color.Red, shape = CircleShape),
                          contentAlignment = Alignment.Center
                      ) {
                          Text(
                              text = unreadMessage.toString(),
                              fontSize = 12.sp,
                              color = Color.White,
                              fontWeight = FontWeight.Bold
                          )
                      }
                  }
              }
          }
      }
}
*/
